/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.io

/** Best-effort delete of a recording's audio file from disk. Never throws. */
expect fun deleteRecordingFile(path: String)
