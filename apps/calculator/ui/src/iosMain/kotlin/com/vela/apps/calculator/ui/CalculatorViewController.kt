/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.ui

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point. The Swift app calls `initCalculatorKoin()` once at launch, then loads this
 * controller. Exposed to Swift as `CalculatorViewControllerKt.calculatorViewController()`.
 */
fun calculatorViewController(): UIViewController = ComposeUIViewController {
    CalculatorApp()
}
