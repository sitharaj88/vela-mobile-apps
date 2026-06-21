/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vela.apps.draw.domain.DrawTool
import com.vela.apps.draw.presentation.DrawIntent
import com.vela.apps.draw.presentation.DrawState
import com.vela.core.designsystem.theme.LocalVelaTokens

/** Vela ink palette plus the canvas background choices. */
private val palette: List<Long> = listOf(
    0xFF1B1B21, // near-black
    0xFFE53935, // red
    0xFF1E88E5, // blue
    0xFF43A047, // green
    0xFFF9A825, // amber
    0xFF8E24AA, // purple
    0xFFFFFFFF, // white (custom ink on dark paper)
)

private val backgrounds: List<Long> = listOf(
    0xFFFFFFFF, // white
    0xFFF5F5F0, // paper
    0xFF1B1B21, // charcoal
    0xFF0D1B2A, // navy
)

private const val MIN_WIDTH = 2f
private const val MAX_WIDTH = 48f

private data class ToolEntry(val tool: DrawTool, val label: String, val icon: ImageVector)

private val tools: List<ToolEntry> = listOf(
    ToolEntry(DrawTool.Brush, "Brush", Icons.Filled.Brush),
    ToolEntry(DrawTool.Eraser, "Eraser", Icons.Filled.Clear),
    ToolEntry(DrawTool.Line, "Line", Icons.Outlined.HorizontalRule),
    ToolEntry(DrawTool.Rectangle, "Rect", Icons.Filled.CropSquare),
    ToolEntry(DrawTool.Ellipse, "Ellipse", Icons.Filled.Circle),
)

@Composable
fun DrawToolbar(
    state: DrawState,
    onIntent: (DrawIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalVelaTokens.current
    Column(
        modifier = modifier.fillMaxWidth().padding(tokens.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
        ) {
            tools.forEach { entry ->
                FilterChip(
                    selected = state.tool == entry.tool,
                    onClick = { onIntent(DrawIntent.SelectTool(entry.tool)) },
                    label = { Text(entry.label) },
                    leadingIcon = { Icon(entry.icon, contentDescription = entry.label) },
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Size", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = state.selectedWidth,
                onValueChange = { onIntent(DrawIntent.SelectWidth(it)) },
                valueRange = MIN_WIDTH..MAX_WIDTH,
                modifier = Modifier.padding(start = tokens.spacing.md).fillMaxWidth(),
            )
        }

        Text("Ink", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
        ) {
            palette.forEach { argb ->
                Swatch(
                    color = Color(argb),
                    selected = state.selectedColorArgb == argb,
                    onClick = { onIntent(DrawIntent.SelectColor(argb)) },
                )
            }
        }

        Text("Paper", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
        ) {
            backgrounds.forEach { argb ->
                Swatch(
                    color = Color(argb),
                    selected = state.backgroundArgb == argb,
                    onClick = { onIntent(DrawIntent.SelectBackground(argb)) },
                )
            }
        }
    }
}

@Composable
private fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val ring = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(if (selected) 3.dp else 1.dp, ring, CircleShape)
            .clickable(onClick = onClick),
    )
}
