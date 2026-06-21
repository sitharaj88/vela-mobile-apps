/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.io

import java.io.File

@Suppress("TooGenericExceptionCaught", "SwallowedException")
actual fun deleteRecordingFile(path: String) {
    try {
        File(path).delete()
    } catch (error: Exception) {
        // Best-effort: a missing/locked file should never crash a delete.
    }
}
