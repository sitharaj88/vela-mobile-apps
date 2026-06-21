/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.android

import android.app.Application
import com.vela.apps.draw.ui.di.initDrawKoin
import org.koin.android.ext.koin.androidContext

class VelaDrawApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initDrawKoin {
            androidContext(this@VelaDrawApp)
        }
    }
}
