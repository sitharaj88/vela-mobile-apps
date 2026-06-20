/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/** How the app resolves light vs dark. */
enum class ThemeMode { System, Light, Dark, Amoled }

/**
 * Root theme for every Vela app. Wraps [MaterialTheme] and provides [LocalVelaTokens].
 *
 * @param accent per-app accent that tints primary roles (neutrals stay shared across the suite).
 * @param themeMode light / dark / amoled / follow-system.
 * @param dynamicColor opt in to Android 12+ wallpaper colors (ignored where unsupported).
 */
@Composable
fun VelaTheme(
    accent: VelaAccent = VelaAccent.Indigo,
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark, ThemeMode.Amoled -> true
    }

    val dynamic = if (dynamicColor) velaDynamicColorScheme(dark) else null
    val base = dynamic ?: if (dark) accent.darkScheme() else accent.lightScheme()
    val colorScheme = if (themeMode == ThemeMode.Amoled) base.toAmoled() else base

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VelaTypography,
        shapes = VelaShapes,
    ) {
        CompositionLocalProvider(LocalVelaTokens provides VelaTokens()) {
            content()
        }
    }
}
