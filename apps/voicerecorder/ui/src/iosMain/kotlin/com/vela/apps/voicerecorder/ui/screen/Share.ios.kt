/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.ui.screen

import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

/**
 * Best-effort iOS share via [UIActivityViewController], presented from the key window's root
 * controller. Presentation plumbing can only be verified on a device, so it is marked TODO(ios).
 */
actual fun shareRecording(filePath: String) {
    // TODO(ios): verify keyWindow/rootViewController presentation on a real device.
    val url = NSURL.fileURLWithPath(filePath)
    val controller = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
    val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    root.presentViewController(controller, animated = true, completion = null)
}
