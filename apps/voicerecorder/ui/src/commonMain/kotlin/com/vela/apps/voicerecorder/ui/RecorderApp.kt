/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.ui

import androidx.compose.runtime.Composable
import com.vela.apps.voicerecorder.ui.screen.RecorderScreen
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme

/**
 * Root composable for Vela Recorder. Platforms call this after starting Koin with
 * [com.vela.apps.voicerecorder.ui.di.recorderModule].
 */
@Composable
fun RecorderApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Rose, themeMode = themeMode, dynamicColor = true) {
        RecorderScreen()
    }
}
