/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.vela.apps.notes.domain.model.NoteColor

/**
 * Maps a [NoteColor] label to a soft container tint for cards/chips.
 *
 * [NoteColor.None] yields the theme surface so unlabeled notes look default; every other label is a
 * low-alpha overlay so it reads well in both light and dark themes without hardcoding opaque colors.
 * The hues mirror the Vela accent palette so labels feel on-brand.
 */
@Composable
@ReadOnlyComposable
fun NoteColor.containerColor(): Color = when (this) {
    NoteColor.None -> MaterialTheme.colorScheme.surface
    else -> swatch().copy(alpha = CONTAINER_ALPHA)
}

/** Full-strength swatch for the color-picker dots and label indicators. */
fun NoteColor.swatch(): Color = when (this) {
    NoteColor.None -> Color.Transparent
    NoteColor.Indigo -> Color(0xFF5B5BD6)
    NoteColor.Teal -> Color(0xFF009688)
    NoteColor.Amber -> Color(0xFFFFB868)
    NoteColor.Rose -> Color(0xFFE57399)
    NoteColor.Forest -> Color(0xFF4CAF50)
    NoteColor.Sky -> Color(0xFF42A5F5)
    NoteColor.Plum -> Color(0xFFAB47BC)
    NoteColor.Crimson -> Color(0xFFE53956)
}

private const val CONTAINER_ALPHA = 0.22f
