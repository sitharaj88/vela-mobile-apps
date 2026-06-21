/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Vela type scale (Material 3 scale, brand-tuned weights/tracking).
 *
 * Uses the platform sans by default; a custom variable font + tabular numerals (for Calculator /
 * Clock) will be wired through Compose Resources without changing call sites.
 */
private val brandSans = FontFamily.SansSerif

private fun vela(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    tracking: Double = 0.0,
) = TextStyle(
    fontFamily = brandSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp,
)

val VelaTypography = Typography(
    displayLarge = vela(FontWeight.Normal, size = 57, lineHeight = 64, tracking = -0.25),
    displayMedium = vela(FontWeight.Normal, size = 45, lineHeight = 52),
    displaySmall = vela(FontWeight.Normal, size = 36, lineHeight = 44),
    headlineLarge = vela(FontWeight.SemiBold, size = 32, lineHeight = 40),
    headlineMedium = vela(FontWeight.SemiBold, size = 28, lineHeight = 36),
    headlineSmall = vela(FontWeight.SemiBold, size = 24, lineHeight = 32),
    titleLarge = vela(FontWeight.SemiBold, size = 22, lineHeight = 28),
    titleMedium = vela(FontWeight.Medium, size = 16, lineHeight = 24, tracking = 0.15),
    titleSmall = vela(FontWeight.Medium, size = 14, lineHeight = 20, tracking = 0.1),
    bodyLarge = vela(FontWeight.Normal, size = 16, lineHeight = 24, tracking = 0.5),
    bodyMedium = vela(FontWeight.Normal, size = 14, lineHeight = 20, tracking = 0.25),
    bodySmall = vela(FontWeight.Normal, size = 12, lineHeight = 16, tracking = 0.4),
    labelLarge = vela(FontWeight.Medium, size = 14, lineHeight = 20, tracking = 0.1),
    labelMedium = vela(FontWeight.Medium, size = 12, lineHeight = 16, tracking = 0.5),
    labelSmall = vela(FontWeight.Medium, size = 11, lineHeight = 16, tracking = 0.5),
)
