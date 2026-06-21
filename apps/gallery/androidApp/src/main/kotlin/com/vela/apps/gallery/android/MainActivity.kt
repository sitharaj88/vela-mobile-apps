/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.vela.apps.gallery.presentation.GalleryIntent
import com.vela.apps.gallery.presentation.GalleryStore
import com.vela.apps.gallery.ui.GalleryApp
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val store: GalleryStore by inject()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        // Re-query once the user responds; granted permissions now expose media to MediaStore.
        store.onIntent(GalleryIntent.Refresh)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        permissionLauncher.launch(requiredPermissions())
        setContent {
            GalleryApp()
        }
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
}
