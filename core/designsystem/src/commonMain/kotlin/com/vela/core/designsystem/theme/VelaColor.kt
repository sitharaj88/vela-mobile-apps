/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Vela's color system.
 *
 * Neutrals (surfaces, outlines, text) are shared across the whole suite so every app feels related.
 * Each app picks a [VelaAccent]; the accent re-tints the primary/secondary/tertiary roles while
 * neutrals stay constant — apps differ without looking unrelated.
 */
enum class VelaAccent(
    private val lightPrimary: Color,
    private val lightSecondary: Color,
    private val lightTertiary: Color,
    private val darkPrimary: Color,
    private val darkSecondary: Color,
    private val darkTertiary: Color,
) {
    /** Brand default — deep indigo-violet. */
    Indigo(
        lightPrimary = Color(0xFF5B5BD6), lightSecondary = Color(0xFF5D5C72), lightTertiary = Color(0xFF7A5368),
        darkPrimary = Color(0xFFBEC2FF), darkSecondary = Color(0xFFC6C4DD), darkTertiary = Color(0xFFE9B9D3),
    ),
    Teal(
        lightPrimary = Color(0xFF006A66), lightSecondary = Color(0xFF4A6361), lightTertiary = Color(0xFF45617A),
        darkPrimary = Color(0xFF53DBD3), darkSecondary = Color(0xFFB1CCC9), darkTertiary = Color(0xFFADCAE6),
    ),
    Amber(
        lightPrimary = Color(0xFF8A5100), lightSecondary = Color(0xFF725A41), lightTertiary = Color(0xFF54643E),
        darkPrimary = Color(0xFFFFB868), darkSecondary = Color(0xFFE1C1A4), darkTertiary = Color(0xFFBBCF9A),
    ),
    Rose(
        lightPrimary = Color(0xFFB01F4F), lightSecondary = Color(0xFF75565C), lightTertiary = Color(0xFF795831),
        darkPrimary = Color(0xFFFFB1C2), darkSecondary = Color(0xFFE5BDC3), darkTertiary = Color(0xFFEBC08D),
    ),
    Forest(
        lightPrimary = Color(0xFF2E6A33), lightSecondary = Color(0xFF53634F), lightTertiary = Color(0xFF38656A),
        darkPrimary = Color(0xFF9BD49A), darkSecondary = Color(0xFFBACAB3), darkTertiary = Color(0xFFA0CFD4),
    ),
    ;

    fun lightScheme(): ColorScheme = lightColorScheme(
        primary = lightPrimary,
        onPrimary = Color.White,
        secondary = lightSecondary,
        onSecondary = Color.White,
        tertiary = lightTertiary,
        onTertiary = Color.White,
        background = NeutralLight.background,
        onBackground = NeutralLight.onBackground,
        surface = NeutralLight.surface,
        onSurface = NeutralLight.onSurface,
        surfaceVariant = NeutralLight.surfaceVariant,
        onSurfaceVariant = NeutralLight.onSurfaceVariant,
        outline = NeutralLight.outline,
        error = ErrorLight,
        onError = Color.White,
    )

    fun darkScheme(): ColorScheme = darkColorScheme(
        primary = darkPrimary,
        onPrimary = Color(0xFF11103A),
        secondary = darkSecondary,
        onSecondary = Color(0xFF2E2D40),
        tertiary = darkTertiary,
        onTertiary = Color(0xFF46263A),
        background = NeutralDark.background,
        onBackground = NeutralDark.onBackground,
        surface = NeutralDark.surface,
        onSurface = NeutralDark.onSurface,
        surfaceVariant = NeutralDark.surfaceVariant,
        onSurfaceVariant = NeutralDark.onSurfaceVariant,
        outline = NeutralDark.outline,
        error = ErrorDark,
        onError = Color(0xFF601410),
    )
}

private object NeutralLight {
    val background = Color(0xFFFBF8FF)
    val onBackground = Color(0xFF1B1B21)
    val surface = Color(0xFFFBF8FF)
    val onSurface = Color(0xFF1B1B21)
    val surfaceVariant = Color(0xFFE3E1EC)
    val onSurfaceVariant = Color(0xFF46464F)
    val outline = Color(0xFF767680)
}

private object NeutralDark {
    val background = Color(0xFF131318)
    val onBackground = Color(0xFFE4E1E9)
    val surface = Color(0xFF131318)
    val onSurface = Color(0xFFE4E1E9)
    val surfaceVariant = Color(0xFF46464F)
    val onSurfaceVariant = Color(0xFFC7C5D0)
    val outline = Color(0xFF91909A)
}

private val ErrorLight = Color(0xFFBA1A1A)
private val ErrorDark = Color(0xFFFFB4AB)

/** True-black variant for AMOLED screens — keeps accent roles, swaps neutrals to black. */
fun ColorScheme.toAmoled(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF1A1A1A),
)
