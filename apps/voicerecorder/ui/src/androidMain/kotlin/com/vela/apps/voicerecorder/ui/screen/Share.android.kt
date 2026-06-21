/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.ui.screen

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.koin.core.context.GlobalContext
import java.io.File

/**
 * Shares a recording via an ACTION_SEND chooser. The private `filesDir` file is exposed through a
 * [FileProvider] (authority `<applicationId>.fileprovider`, declared in the app manifest). The
 * application [Context] is pulled from the already-started Koin container.
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
actual fun shareRecording(filePath: String) {
    val context = GlobalContext.getOrNull()?.get<Context>() ?: return
    try {
        val file = File(filePath)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Share recording")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (error: Exception) {
        // Best-effort share — never crash the UI if no handler/provider is configured.
    }
}
