/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.android

import android.app.Application
import com.vela.apps.applauncher.ui.di.initAppLauncherKoin
import org.koin.android.ext.koin.androidContext

class VelaAppLauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initAppLauncherKoin {
            androidContext(this@VelaAppLauncherApp)
        }
    }
}
