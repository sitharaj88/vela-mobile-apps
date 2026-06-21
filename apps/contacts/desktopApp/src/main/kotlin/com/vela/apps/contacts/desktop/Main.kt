/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vela.apps.contacts.ui.ContactsApp
import com.vela.apps.contacts.ui.di.initContactsKoin

fun main() {
    initContactsKoin()
    application {
        val windowState = rememberWindowState(size = DpSize(460.dp, 760.dp))
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Vela Contacts",
        ) {
            ContactsApp()
        }
    }
}
