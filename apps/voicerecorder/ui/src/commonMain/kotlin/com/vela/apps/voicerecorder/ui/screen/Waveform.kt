/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

private const val MIN_BAR_FRACTION = 0.06f
private const val BAR_WIDTH_PX = 6f
private const val BAR_GAP_PX = 4f

/**
 * Live capture waveform: each recent amplitude (0f..1f) is drawn as a vertical bar growing from the
 * vertical center. Newest samples are on the right; the list scrolls as it fills. Colors come from
 * the theme so the meter follows the accent.
 */
@Composable
internal fun Waveform(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier.fillMaxWidth().height(72.dp)) {
        val centerY = size.height / 2f
        val step = BAR_WIDTH_PX + BAR_GAP_PX
        val capacity = (size.width / step).toInt().coerceAtLeast(1)
        val visible = amplitudes.takeLast(capacity)
        // Right-align so the newest bar hugs the trailing edge.
        val startX = size.width - visible.size * step
        visible.forEachIndexed { index, amplitude ->
            val level = amplitude.coerceIn(0f, 1f).coerceAtLeast(MIN_BAR_FRACTION)
            val half = centerY * level
            val x = startX + index * step + BAR_WIDTH_PX / 2f
            drawLine(
                color = color,
                start = Offset(x, centerY - half),
                end = Offset(x, centerY + half),
                strokeWidth = BAR_WIDTH_PX,
                cap = StrokeCap.Round,
            )
        }
    }
}
