/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.ui

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point — exposed to Swift as
 * `AppLauncherViewControllerKt.appLauncherViewController()`.
 */
fun appLauncherViewController(): UIViewController = ComposeUIViewController {
    AppLauncherApp()
}
