/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RenderCommandTest {

    @Test
    fun single_point_stroke_becomes_a_dot() {
        val commands = renderCommandsOf(listOf(Stroke(listOf(StrokePoint(3f, 4f)), 0xFF000000, 6f)))
        val dot = assertIs<RenderCommand.Dot>(commands.single())
        assertEquals(3f, dot.cx)
        assertEquals(4f, dot.cy)
    }

    @Test
    fun multi_point_stroke_becomes_a_polyline() {
        val stroke = Stroke(listOf(StrokePoint(0f, 0f), StrokePoint(1f, 1f)), 0xFF112233, 8f)
        val command = assertIs<RenderCommand.PolyLine>(renderCommandsOf(listOf(stroke)).single())
        assertEquals(2, command.points.size)
    }

    @Test
    fun empty_stroke_produces_no_command() {
        assertTrue(renderCommandsOf(listOf(Stroke(emptyList(), 0xFF000000, 8f))).isEmpty())
    }

    @Test
    fun rectangle_bounds_are_normalized_regardless_of_drag_direction() {
        val shape = ShapeElement(DrawTool.Rectangle, StrokePoint(30f, 40f), StrokePoint(10f, 20f), 0xFF000000, 4f)
        val rect = assertIs<RenderCommand.Rect>(renderCommandsOf(listOf(shape)).single())
        assertEquals(10f, rect.left)
        assertEquals(20f, rect.top)
        assertEquals(30f, rect.right)
        assertEquals(40f, rect.bottom)
    }

    @Test
    fun ellipse_maps_to_ellipse_command() {
        val shape = ShapeElement(DrawTool.Ellipse, StrokePoint(0f, 0f), StrokePoint(10f, 10f), 0xFF000000, 4f)
        assertIs<RenderCommand.Ellipse>(renderCommandsOf(listOf(shape)).single())
    }

    @Test
    fun line_preserves_raw_endpoints() {
        val shape = ShapeElement(DrawTool.Line, StrokePoint(5f, 9f), StrokePoint(1f, 2f), 0xFF000000, 4f)
        val line = assertIs<RenderCommand.Line>(renderCommandsOf(listOf(shape)).single())
        assertEquals(5f, line.x1)
        assertEquals(9f, line.y1)
        assertEquals(1f, line.x2)
        assertEquals(2f, line.y2)
    }

    @Test
    fun ordering_is_preserved() {
        val elements = listOf(
            Stroke(listOf(StrokePoint(0f, 0f), StrokePoint(1f, 1f)), 0xFF000000, 8f),
            ShapeElement(DrawTool.Rectangle, StrokePoint(0f, 0f), StrokePoint(2f, 2f), 0xFF000000, 4f),
        )
        val commands = renderCommandsOf(elements)
        assertIs<RenderCommand.PolyLine>(commands[0])
        assertIs<RenderCommand.Rect>(commands[1])
    }
}
