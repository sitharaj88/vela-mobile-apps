/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.domain

/**
 * A drawing primitive, framework-free so it lives in the domain. The UI maps [StrokePoint] to a
 * Compose `Offset` and [colorArgb] to a Compose `Color` at render time.
 */
data class StrokePoint(val x: Float, val y: Float)

/**
 * The tools the user can paint with. [Brush] and [Eraser] are freehand (recorded as point lists);
 * [Line], [Rectangle] and [Ellipse] are dragged from a start corner to an end corner.
 */
enum class DrawTool {
    Brush,
    Eraser,
    Line,
    Rectangle,
    Ellipse,
}

/**
 * One committed item on the canvas. Everything the user draws becomes a [DrawElement] so a single
 * undo/redo history and a single render pass cover both freehand strokes and geometric shapes.
 */
sealed interface DrawElement {
    val colorArgb: Long
    val width: Float
}

/** A freehand stroke (brush or eraser). An eraser is just a stroke painted in the background color. */
data class Stroke(
    val points: List<StrokePoint>,
    override val colorArgb: Long,
    override val width: Float,
) : DrawElement

/** A geometric shape dragged between [start] and [end] (line, rectangle or ellipse). */
data class ShapeElement(
    val tool: DrawTool,
    val start: StrokePoint,
    val end: StrokePoint,
    override val colorArgb: Long,
    override val width: Float,
) : DrawElement
