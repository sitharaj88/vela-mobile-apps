/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Returns the platform's wallpaper-derived dynamic color scheme, or null when unavailable.
 * Only Android 12+ supports this; every other target returns null.
 */
@Composable
expect fun velaDynamicColorScheme(dark: Boolean): ColorScheme?
