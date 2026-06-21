/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.android

import android.app.Application
import com.vela.apps.voicerecorder.ui.di.initRecorderKoin
import org.koin.android.ext.koin.androidContext

class VelaRecorderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initRecorderKoin {
            androidContext(this@VelaRecorderApp)
        }
    }
}
