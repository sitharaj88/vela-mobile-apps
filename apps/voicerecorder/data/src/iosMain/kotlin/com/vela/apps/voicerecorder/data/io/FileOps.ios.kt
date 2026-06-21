/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.io

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager

@OptIn(ExperimentalForeignApi::class)
actual fun deleteRecordingFile(path: String) {
    // removeItemAtPath returns false on error; we ignore it (best-effort).
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}
