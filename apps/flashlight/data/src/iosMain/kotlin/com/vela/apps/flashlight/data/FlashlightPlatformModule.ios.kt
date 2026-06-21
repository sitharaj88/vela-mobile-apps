/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.data

import com.vela.apps.flashlight.domain.Torch
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.lockForConfiguration
import platform.AVFoundation.setTorchMode
import platform.AVFoundation.unlockForConfiguration

/**
 * iOS torch backed by [AVCaptureDevice]. Best-effort: torch control needs the device to be locked
 * for configuration. Uncertain cinterop bindings are marked `TODO(ios)` and only compile on macOS.
 */
private class IosTorch : Torch {

    // TODO(ios): defaultDeviceWithMediaType may need a video device discovery session on newer iOS.
    private val device: AVCaptureDevice? =
        AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)

    override val isAvailable: Boolean = device?.hasTorch() == true

    override fun setEnabled(on: Boolean) {
        val captureDevice = device ?: return
        if (!captureDevice.isTorchAvailable()) return
        // TODO(ios): error pointer passed as null; consider surfacing lock failures.
        if (captureDevice.lockForConfiguration(null)) {
            captureDevice.setTorchMode(if (on) AVCaptureTorchModeOn else AVCaptureTorchModeOff)
            captureDevice.unlockForConfiguration()
        }
    }
}

actual fun flashlightPlatformModule(): Module = module {
    single<Torch> { IosTorch() }
}
