/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.android

import android.app.Application
import com.vela.apps.flashlight.ui.di.initFlashlightKoin
import org.koin.android.ext.koin.androidContext

class VelaFlashlightApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initFlashlightKoin {
            androidContext(this@VelaFlashlightApp)
        }
    }
}
