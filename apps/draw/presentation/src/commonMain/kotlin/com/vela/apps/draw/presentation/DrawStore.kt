/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.presentation

import androidx.lifecycle.viewModelScope
import com.vela.apps.draw.domain.DrawElement
import com.vela.apps.draw.domain.Drawing
import com.vela.apps.draw.domain.ImageExporter
import com.vela.apps.draw.domain.ShapeElement
import com.vela.apps.draw.domain.Stroke
import com.vela.core.common.MviStore
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

/**
 * Holds the committed [DrawElement]s plus the current tool/color/width/background selection, manages
 * an undo/redo history over those elements, and drives PNG export through an [ImageExporter].
 *
 * Edits are pure synchronous state transitions; only [DrawIntent.Export] launches a coroutine.
 */
class DrawStore(private val exporter: ImageExporter) :
    MviStore<DrawState, DrawIntent, DrawEffect>(DrawState()) {

    // Elements popped by Undo, available to Redo until the next fresh edit.
    private val redoStack = ArrayDeque<DrawElement>()

    override fun onIntent(intent: DrawIntent) {
        when (intent) {
            is DrawIntent.CommitStroke -> commitStroke(intent)
            is DrawIntent.CommitShape -> commitShape(intent)
            is DrawIntent.SelectTool -> setState { copy(tool = intent.tool) }
            is DrawIntent.SelectColor -> setState { copy(selectedColorArgb = intent.argb) }
            is DrawIntent.SelectWidth -> setState { copy(selectedWidth = intent.width) }
            is DrawIntent.SelectBackground -> setState { copy(backgroundArgb = intent.argb) }
            DrawIntent.Undo -> undo()
            DrawIntent.Redo -> redo()
            DrawIntent.Clear -> clear()
            is DrawIntent.Export -> export(intent)
        }
    }

    private fun commitStroke(intent: DrawIntent.CommitStroke) {
        if (intent.points.isEmpty()) return
        add(Stroke(intent.points, currentState.activeColorArgb, currentState.selectedWidth))
    }

    private fun commitShape(intent: DrawIntent.CommitShape) {
        if (intent.start == intent.end) return
        add(
            ShapeElement(
                tool = currentState.tool,
                start = intent.start,
                end = intent.end,
                colorArgb = currentState.activeColorArgb,
                width = currentState.selectedWidth,
            ),
        )
    }

    private fun add(element: DrawElement) {
        redoStack.clear()
        setState { copy(elements = elements.add(element)) }
        refreshFlags()
    }

    private fun undo() {
        val last = currentState.elements.lastOrNull() ?: return
        redoStack.addLast(last)
        setState { copy(elements = elements.removeAt(elements.lastIndex)) }
        refreshFlags()
    }

    private fun redo() {
        val element = redoStack.removeLastOrNull() ?: return
        setState { copy(elements = elements.add(element)) }
        refreshFlags()
    }

    private fun clear() {
        if (currentState.elements.isEmpty()) return
        redoStack.clear()
        setState { copy(elements = persistentListOf()) }
        refreshFlags()
    }

    private fun export(intent: DrawIntent.Export) {
        if (currentState.isExporting || intent.width <= 0 || intent.height <= 0) return
        val drawing = Drawing(
            width = intent.width,
            height = intent.height,
            backgroundArgb = currentState.backgroundArgb,
            elements = currentState.elements.toList(),
        )
        setState { copy(isExporting = true) }
        viewModelScope.launch {
            val effect = runCatching { exporter.exportPng(drawing, FILE_NAME) }
                .fold(
                    onSuccess = { DrawEffect.Exported(it.location) },
                    onFailure = { DrawEffect.ExportFailed(it.message ?: "Export failed") },
                )
            setState { copy(isExporting = false) }
            emitEffect(effect)
        }
    }

    private fun refreshFlags() = setState {
        copy(canUndo = elements.isNotEmpty(), canRedo = redoStack.isNotEmpty())
    }

    private companion object {
        /** Base name; platform exporters append a timestamp + ".png" to keep saves unique. */
        const val FILE_NAME = "vela-draw"
    }
}
