/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.ui.screen

import java.awt.Desktop
import java.io.File

/** Desktop "share" opens the containing folder so the user can attach the WAV themselves. */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
actual fun shareRecording(filePath: String) {
    try {
        val file = File(filePath)
        val target = if (file.exists()) file.parentFile else null
        if (target != null && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(target)
        }
    } catch (error: Exception) {
        // Best-effort — headless or unsupported desktops simply do nothing.
    }
}
