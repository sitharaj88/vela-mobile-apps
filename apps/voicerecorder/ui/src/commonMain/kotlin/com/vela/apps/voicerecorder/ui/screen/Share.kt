/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.ui.screen

/**
 * Hands a recording file off to the platform share sheet. Android uses an ACTION_SEND chooser; iOS
 * a UIActivityViewController (best-effort); desktop opens the file's folder. No-op if unavailable.
 */
expect fun shareRecording(filePath: String)
