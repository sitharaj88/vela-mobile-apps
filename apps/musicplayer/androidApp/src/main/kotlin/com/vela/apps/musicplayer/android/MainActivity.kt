/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.vela.apps.musicplayer.presentation.LibraryIntent
import com.vela.apps.musicplayer.presentation.LibraryStore
import com.vela.apps.musicplayer.ui.MusicApp
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val libraryStore: LibraryStore by inject()

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        // Re-query the library once the user responds so freshly granted reads are picked up.
        libraryStore.onIntent(LibraryIntent.Reload)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestPermission.launch(audioPermission())
        setContent {
            MusicApp()
        }
    }

    private fun audioPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
}
