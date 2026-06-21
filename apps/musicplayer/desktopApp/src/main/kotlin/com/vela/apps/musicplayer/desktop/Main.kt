/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vela.apps.musicplayer.ui.MusicApp
import com.vela.apps.musicplayer.ui.di.initMusicKoin

fun main() {
    initMusicKoin()
    application {
        val windowState = rememberWindowState(size = DpSize(440.dp, 760.dp))
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Vela Music",
        ) {
            MusicApp()
        }
    }
}
