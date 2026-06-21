/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.vela.apps.voicerecorder.ui.RecorderApp

class MainActivity : ComponentActivity() {

    // Recorder fails gracefully if the permission is denied, so we ignore the result.
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestRecordAudioIfNeeded()
        setContent {
            RecorderApp()
        }
    }

    private fun requestRecordAudioIfNeeded() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
