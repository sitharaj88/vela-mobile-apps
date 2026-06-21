/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.ui

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** iOS entry point — exposed to Swift as `ContactsViewControllerKt.contactsViewController()`. */
fun contactsViewController(): UIViewController = ComposeUIViewController {
    ContactsApp()
}
