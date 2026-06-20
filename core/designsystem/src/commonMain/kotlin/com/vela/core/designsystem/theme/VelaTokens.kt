/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Brand tokens that extend [androidx.compose.material3.MaterialTheme] with Vela-specific values
 * that Material does not model. UI reads these via [LocalVelaTokens] — never hardcode dimensions.
 */
@Immutable
data class VelaTokens(
    val spacing: VelaSpacing = VelaSpacing(),
    val motion: VelaMotion = VelaMotion(),
    val elevation: VelaElevation = VelaElevation(),
)

/** 4dp baseline grid. */
@Immutable
data class VelaSpacing(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
)

/** Motion spec — durations in millis + emphasized easings (Material 3 Expressive). */
@Immutable
data class VelaMotion(
    val durationFast: Int = 150,
    val durationMedium: Int = 300,
    val durationSlow: Int = 450,
    val emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
    val emphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f),
    val emphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f),
)

@Immutable
data class VelaElevation(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 3.dp,
    val level3: Dp = 6.dp,
)

val LocalVelaTokens = staticCompositionLocalOf { VelaTokens() }
