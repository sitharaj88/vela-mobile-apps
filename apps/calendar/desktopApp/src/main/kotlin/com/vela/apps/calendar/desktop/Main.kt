/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vela.apps.calendar.ui.CalendarApp
import com.vela.apps.calendar.ui.di.initCalendarKoin

fun main() {
    initCalendarKoin()
    application {
        val windowState = rememberWindowState(size = DpSize(460.dp, 760.dp))
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Vela Calendar",
        ) {
            CalendarApp()
        }
    }
}
