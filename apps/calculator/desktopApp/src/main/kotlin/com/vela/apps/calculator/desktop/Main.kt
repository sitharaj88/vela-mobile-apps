/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vela.apps.calculator.ui.CalculatorApp
import com.vela.apps.calculator.ui.di.initCalculatorKoin

fun main() {
    initCalculatorKoin()
    application {
        val windowState = rememberWindowState(size = DpSize(420.dp, 720.dp))
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Vela Calculator",
        ) {
            CalculatorApp()
        }
    }
}
