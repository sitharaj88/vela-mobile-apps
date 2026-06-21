/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.data

import android.content.Context
import android.hardware.camera2.CameraManager
import com.vela.apps.flashlight.domain.Torch
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android torch backed by [CameraManager.setTorchMode]. `setTorchMode` requires no runtime
 * permission. We bind to the first camera that advertises a flash unit.
 */
private class AndroidTorch(private val cameraManager: CameraManager) : Torch {

    private val flashCameraId: String? = runCatching {
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }.getOrNull()

    override val isAvailable: Boolean = flashCameraId != null

    override fun setEnabled(on: Boolean) {
        val id = flashCameraId ?: return
        runCatching { cameraManager.setTorchMode(id, on) }
    }
}

actual fun flashlightPlatformModule(): Module = module {
    single<Torch> {
        val context = androidContext().applicationContext
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        AndroidTorch(cameraManager)
    }
}
