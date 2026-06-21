/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.domain

/**
 * A flat, geometry-resolved drawing instruction. Turning the [Drawing] model into these once (via
 * [renderCommandsOf]) keeps the per-platform rasterizers tiny: they only translate each command
 * into native pen/canvas calls, never re-deriving shape bounds or eraser semantics.
 */
sealed interface RenderCommand {
    val colorArgb: Long
    val width: Float

    /** A single dab — a one-point stroke renders as a filled dot of radius [width] / 2. */
    data class Dot(val cx: Float, val cy: Float, override val colorArgb: Long, override val width: Float) :
        RenderCommand

    /** A connected poly-line through [points] (freehand brush / eraser path). */
    data class PolyLine(val points: List<StrokePoint>, override val colorArgb: Long, override val width: Float) :
        RenderCommand

    /** A straight segment between two points. */
    data class Line(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        override val colorArgb: Long,
        override val width: Float,
    ) : RenderCommand

    /** A stroked rectangle in normalized (left/top/right/bottom) coordinates. */
    data class Rect(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        override val colorArgb: Long,
        override val width: Float,
    ) : RenderCommand

    /** A stroked ellipse inscribed in the normalized (left/top/right/bottom) box. */
    data class Ellipse(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        override val colorArgb: Long,
        override val width: Float,
    ) : RenderCommand
}

/** Flattens a [Drawing]'s elements into ordered, geometry-resolved [RenderCommand]s. */
fun renderCommandsOf(elements: List<DrawElement>): List<RenderCommand> =
    elements.mapNotNull { element ->
        when (element) {
            is Stroke -> strokeCommand(element)
            is ShapeElement -> shapeCommand(element)
        }
    }

private fun strokeCommand(stroke: Stroke): RenderCommand? = when (stroke.points.size) {
    0 -> null
    1 -> RenderCommand.Dot(stroke.points[0].x, stroke.points[0].y, stroke.colorArgb, stroke.width)
    else -> RenderCommand.PolyLine(stroke.points, stroke.colorArgb, stroke.width)
}

private fun shapeCommand(shape: ShapeElement): RenderCommand {
    val left = minOf(shape.start.x, shape.end.x)
    val top = minOf(shape.start.y, shape.end.y)
    val right = maxOf(shape.start.x, shape.end.x)
    val bottom = maxOf(shape.start.y, shape.end.y)
    return when (shape.tool) {
        DrawTool.Line -> RenderCommand.Line(
            shape.start.x, shape.start.y, shape.end.x, shape.end.y, shape.colorArgb, shape.width,
        )
        DrawTool.Ellipse -> RenderCommand.Ellipse(left, top, right, bottom, shape.colorArgb, shape.width)
        else -> RenderCommand.Rect(left, top, right, bottom, shape.colorArgb, shape.width)
    }
}
