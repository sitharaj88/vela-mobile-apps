/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.android

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vela.apps.contacts.ui.ContactsApp

/**
 * Requests `READ_CONTACTS` at launch. The Contacts store reads the provider when the list screen's
 * ViewModel is created, so we only show [ContactsApp] after the permission prompt resolves — this
 * way the first query runs with the permission state already decided (granted or denied).
 */
class MainActivity : ComponentActivity() {

    private var permissionResolved by mutableStateOf(false)

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { permissionResolved = true }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestPermission.launch(Manifest.permission.READ_CONTACTS)
        setContent {
            // Touch the flag so the composition recomposes once the prompt resolves.
            permissionResolved
            ContactsApp()
        }
    }
}
