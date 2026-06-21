/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vela.apps.applauncher.ui.AppLauncherApp
import com.vela.apps.applauncher.ui.di.initAppLauncherKoin

fun main() {
    initAppLauncherKoin()
    application {
        val windowState = rememberWindowState(size = DpSize(440.dp, 760.dp))
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Vela Apps",
        ) {
            AppLauncherApp()
        }
    }
}
