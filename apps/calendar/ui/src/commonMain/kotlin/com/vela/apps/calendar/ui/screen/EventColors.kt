/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.screen

import androidx.compose.ui.graphics.Color
import com.vela.apps.calendar.domain.model.Event

/**
 * A small fixed, accessible palette for event color labels. These are semantic data carried on the
 * event (not theme chrome), so a fixed hue per index is intentional and stays consistent across
 * platforms, light/dark, and dynamic color. The default index 0 mirrors the app's indigo accent.
 */
private val labelPalette = listOf(
    Color(0xFF6366F1), // indigo (default, mirrors the app accent)
    Color(0xFF2563EB), // blue
    Color(0xFF059669), // emerald
    Color(0xFFD97706), // amber
    Color(0xFFDC2626), // red
    Color(0xFF7C3AED), // violet
)

/** Resolve an [Event.colorIndex] to its label color (wraps safely on out-of-range indices). */
fun eventLabelColor(colorIndex: Int): Color {
    val index = ((colorIndex % Event.COLOR_COUNT) + Event.COLOR_COUNT) % Event.COLOR_COUNT
    return labelPalette[index]
}
