/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.android

import android.app.Application
import com.vela.apps.filemanager.ui.di.initFileManagerKoin
import org.koin.android.ext.koin.androidContext

class VelaFileManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initFileManagerKoin {
            androidContext(this@VelaFileManagerApp)
        }
    }
}
