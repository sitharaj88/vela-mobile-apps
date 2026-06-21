/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.presentation

import com.vela.apps.draw.domain.DrawElement
import com.vela.apps.draw.domain.DrawTool
import com.vela.apps.draw.domain.StrokePoint
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

/** Default ink: near-black, medium width, white paper. */
const val DEFAULT_COLOR_ARGB: Long = 0xFF1B1B21
const val DEFAULT_BACKGROUND_ARGB: Long = 0xFFFFFFFF
const val DEFAULT_STROKE_WIDTH: Float = 8f

data class DrawState(
    val elements: PersistentList<DrawElement> = persistentListOf(),
    val tool: DrawTool = DrawTool.Brush,
    val selectedColorArgb: Long = DEFAULT_COLOR_ARGB,
    val backgroundArgb: Long = DEFAULT_BACKGROUND_ARGB,
    val selectedWidth: Float = DEFAULT_STROKE_WIDTH,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isExporting: Boolean = false,
) {
    /** The eraser paints in the paper color so it "removes" ink visually. */
    val activeColorArgb: Long get() = if (tool == DrawTool.Eraser) backgroundArgb else selectedColorArgb
}

sealed interface DrawIntent {
    /** Commit a finished freehand stroke (the live in-progress stroke is held in the UI). */
    data class CommitStroke(val points: List<StrokePoint>) : DrawIntent

    /** Commit a finished shape dragged between two points. */
    data class CommitShape(val start: StrokePoint, val end: StrokePoint) : DrawIntent

    data class SelectTool(val tool: DrawTool) : DrawIntent
    data class SelectColor(val argb: Long) : DrawIntent
    data class SelectWidth(val width: Float) : DrawIntent
    data class SelectBackground(val argb: Long) : DrawIntent
    data object Undo : DrawIntent
    data object Redo : DrawIntent
    data object Clear : DrawIntent

    /** Render the current drawing at the given canvas pixel size and save it as PNG. */
    data class Export(val width: Int, val height: Int) : DrawIntent
}

/** One-shot events for the UI (snackbars). */
sealed interface DrawEffect {
    data class Exported(val location: String) : DrawEffect
    data class ExportFailed(val message: String) : DrawEffect
}
