/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.presentation

import app.cash.turbine.test
import com.vela.apps.draw.domain.Drawing
import com.vela.apps.draw.domain.ExportResult
import com.vela.apps.draw.domain.ImageExporter
import com.vela.apps.draw.domain.ShapeElement
import com.vela.apps.draw.domain.Stroke
import com.vela.apps.draw.domain.DrawTool
import com.vela.apps.draw.domain.StrokePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Records the last drawing passed to [exportPng] and lets a test force a failure. */
private class FakeExporter(private val failWith: String? = null) : ImageExporter {
    var lastDrawing: Drawing? = null
    var calls = 0

    override suspend fun exportPng(drawing: Drawing, fileName: String): ExportResult {
        calls++
        lastDrawing = drawing
        failWith?.let { throw IllegalStateException(it) }
        return ExportResult("/tmp/$fileName.png")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DrawStoreTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun store(exporter: ImageExporter = FakeExporter()) = DrawStore(exporter)

    private fun points(vararg xs: Float) = xs.map { StrokePoint(it, it) }

    @Test
    fun commit_adds_stroke_and_enables_undo() {
        val store = store()
        store.onIntent(DrawIntent.CommitStroke(points(0f, 1f, 2f)))
        val s = store.state.value
        assertEquals(1, s.elements.size)
        assertTrue(s.canUndo)
        assertFalse(s.canRedo)
    }

    @Test
    fun empty_stroke_is_ignored() {
        val store = store()
        store.onIntent(DrawIntent.CommitStroke(emptyList()))
        assertEquals(0, store.state.value.elements.size)
    }

    @Test
    fun commit_shape_adds_shape_element() {
        val store = store()
        store.onIntent(DrawIntent.SelectTool(DrawTool.Rectangle))
        store.onIntent(DrawIntent.CommitShape(StrokePoint(0f, 0f), StrokePoint(10f, 20f)))
        val element = store.state.value.elements.single()
        assertIs<ShapeElement>(element)
        assertEquals(DrawTool.Rectangle, element.tool)
    }

    @Test
    fun degenerate_shape_is_ignored() {
        val store = store()
        store.onIntent(DrawIntent.SelectTool(DrawTool.Line))
        store.onIntent(DrawIntent.CommitShape(StrokePoint(5f, 5f), StrokePoint(5f, 5f)))
        assertEquals(0, store.state.value.elements.size)
    }

    @Test
    fun eraser_commits_in_background_color() {
        val store = store()
        store.onIntent(DrawIntent.SelectBackground(0xFF000000))
        store.onIntent(DrawIntent.SelectTool(DrawTool.Eraser))
        store.onIntent(DrawIntent.CommitStroke(points(0f, 1f)))
        val stroke = store.state.value.elements.single()
        assertIs<Stroke>(stroke)
        assertEquals(0xFF000000, stroke.colorArgb)
    }

    @Test
    fun undo_then_redo_round_trips() {
        val store = store()
        store.onIntent(DrawIntent.CommitStroke(points(0f, 1f)))
        store.onIntent(DrawIntent.Undo)
        assertEquals(0, store.state.value.elements.size)
        assertTrue(store.state.value.canRedo)

        store.onIntent(DrawIntent.Redo)
        assertEquals(1, store.state.value.elements.size)
        assertFalse(store.state.value.canRedo)
    }

    @Test
    fun new_element_after_undo_clears_redo() {
        val store = store()
        store.onIntent(DrawIntent.CommitStroke(points(0f, 1f)))
        store.onIntent(DrawIntent.Undo)
        store.onIntent(DrawIntent.CommitStroke(points(2f, 3f)))
        assertFalse(store.state.value.canRedo)
        assertEquals(1, store.state.value.elements.size)
    }

    @Test
    fun clear_removes_everything() {
        val store = store()
        store.onIntent(DrawIntent.CommitStroke(points(0f, 1f)))
        store.onIntent(DrawIntent.CommitStroke(points(2f, 3f)))
        store.onIntent(DrawIntent.Clear)
        assertEquals(0, store.state.value.elements.size)
        assertFalse(store.state.value.canUndo)
    }

    @Test
    fun selecting_tool_color_width_background_updates_state() {
        val store = store()
        store.onIntent(DrawIntent.SelectTool(DrawTool.Ellipse))
        store.onIntent(DrawIntent.SelectColor(0xFFE53935))
        store.onIntent(DrawIntent.SelectWidth(16f))
        store.onIntent(DrawIntent.SelectBackground(0xFF101010))
        val s = store.state.value
        assertEquals(DrawTool.Ellipse, s.tool)
        assertEquals(0xFFE53935, s.selectedColorArgb)
        assertEquals(16f, s.selectedWidth)
        assertEquals(0xFF101010, s.backgroundArgb)
    }

    @Test
    fun export_passes_drawing_and_emits_success() = runTest(dispatcher) {
        val exporter = FakeExporter()
        val store = store(exporter)
        store.onIntent(DrawIntent.CommitStroke(points(0f, 1f)))
        store.onIntent(DrawIntent.SelectBackground(0xFFFAFAFA))

        store.effects.test {
            store.onIntent(DrawIntent.Export(width = 200, height = 100))
            testScheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertIs<DrawEffect.Exported>(effect)
        }
        assertEquals(1, exporter.calls)
        assertEquals(200, exporter.lastDrawing?.width)
        assertEquals(100, exporter.lastDrawing?.height)
        assertEquals(0xFFFAFAFA, exporter.lastDrawing?.backgroundArgb)
        assertFalse(store.state.value.isExporting)
    }

    @Test
    fun export_failure_emits_failed_effect() = runTest(dispatcher) {
        val store = store(FakeExporter(failWith = "disk full"))
        store.effects.test {
            store.onIntent(DrawIntent.Export(width = 10, height = 10))
            testScheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertIs<DrawEffect.ExportFailed>(effect)
            assertEquals("disk full", effect.message)
        }
        assertFalse(store.state.value.isExporting)
    }

    @Test
    fun export_with_zero_size_is_ignored() = runTest(dispatcher) {
        val exporter = FakeExporter()
        val store = store(exporter)
        store.onIntent(DrawIntent.Export(width = 0, height = 0))
        testScheduler.advanceUntilIdle()
        assertEquals(0, exporter.calls)
    }
}
