/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.ui

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** iOS entry point — exposed to Swift as `DrawViewControllerKt.drawViewController()`. */
fun drawViewController(): UIViewController = ComposeUIViewController {
    DrawApp()
}
