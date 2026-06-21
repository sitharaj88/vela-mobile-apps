/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as StrokeStyle
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.vela.apps.draw.domain.DrawElement
import com.vela.apps.draw.domain.DrawTool
import com.vela.apps.draw.domain.RenderCommand
import com.vela.apps.draw.domain.ShapeElement
import com.vela.apps.draw.domain.Stroke
import com.vela.apps.draw.domain.StrokePoint
import com.vela.apps.draw.domain.renderCommandsOf
import com.vela.apps.draw.presentation.DrawState

/**
 * The drawing surface. Committed elements come from [state]; the in-progress gesture is kept in
 * local state for performance and only sent up on drag end. Freehand tools build a point list;
 * shape tools track a start/end pair. [onSize] reports the pixel size so export matches the canvas.
 */
@Composable
fun DrawCanvas(
    state: DrawState,
    onCommitStroke: (List<StrokePoint>) -> Unit,
    onCommitShape: (StrokePoint, StrokePoint) -> Unit,
    onSize: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var livePoints by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }
    var shapeStart by remember { mutableStateOf<StrokePoint?>(null) }
    var shapeEnd by remember { mutableStateOf<StrokePoint?>(null) }
    val freehand = state.tool == DrawTool.Brush || state.tool == DrawTool.Eraser

    Canvas(
        modifier = modifier
            .background(Color(state.backgroundArgb))
            .onSizeChanged { onSize(it.width, it.height) }
            .pointerInput(state.tool) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (freehand) {
                            livePoints = listOf(StrokePoint(offset.x, offset.y))
                        } else {
                            shapeStart = StrokePoint(offset.x, offset.y)
                            shapeEnd = shapeStart
                        }
                    },
                    onDrag = { change, _ ->
                        val point = StrokePoint(change.position.x, change.position.y)
                        if (freehand) livePoints = livePoints + point else shapeEnd = point
                    },
                    onDragEnd = {
                        if (freehand) {
                            onCommitStroke(livePoints)
                            livePoints = emptyList()
                        } else {
                            val start = shapeStart
                            val end = shapeEnd
                            if (start != null && end != null) onCommitShape(start, end)
                            shapeStart = null
                            shapeEnd = null
                        }
                    },
                    onDragCancel = {
                        livePoints = emptyList()
                        shapeStart = null
                        shapeEnd = null
                    },
                )
            },
    ) {
        renderCommandsOf(state.elements).forEach { drawCommand(it) }
        drawLivePreview(state, livePoints, shapeStart, shapeEnd)
    }
}

private fun DrawScope.drawLivePreview(
    state: DrawState,
    livePoints: List<StrokePoint>,
    shapeStart: StrokePoint?,
    shapeEnd: StrokePoint?,
) {
    val color = state.activeColorArgb
    if (livePoints.isNotEmpty()) {
        renderCommandsOf(listOf(Stroke(livePoints, color, state.selectedWidth))).forEach { drawCommand(it) }
    }
    if (shapeStart != null && shapeEnd != null) {
        val preview: DrawElement = ShapeElement(state.tool, shapeStart, shapeEnd, color, state.selectedWidth)
        renderCommandsOf(listOf(preview)).forEach { drawCommand(it) }
    }
}

private fun DrawScope.drawCommand(command: RenderCommand) {
    val color = Color(command.colorArgb)
    val style = StrokeStyle(width = command.width, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when (command) {
        is RenderCommand.Dot -> drawCircle(color, command.width / 2f, Offset(command.cx, command.cy))
        is RenderCommand.PolyLine -> {
            val path = Path().apply {
                moveTo(command.points.first().x, command.points.first().y)
                for (i in 1 until command.points.size) lineTo(command.points[i].x, command.points[i].y)
            }
            drawPath(path, color, style = style)
        }
        is RenderCommand.Line ->
            drawLine(
                color,
                start = Offset(command.x1, command.y1),
                end = Offset(command.x2, command.y2),
                strokeWidth = command.width,
                cap = StrokeCap.Round,
            )
        is RenderCommand.Rect ->
            drawRect(
                color,
                topLeft = Offset(command.left, command.top),
                size = Size(command.right - command.left, command.bottom - command.top),
                style = style,
            )
        is RenderCommand.Ellipse ->
            drawOval(
                color,
                topLeft = Offset(command.left, command.top),
                size = Size(command.right - command.left, command.bottom - command.top),
                style = style,
            )
    }
}
