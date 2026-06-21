/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.android

import android.app.Application
import com.vela.apps.notes.ui.di.initNotesKoin
import org.koin.android.ext.koin.androidContext

class VelaNotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initNotesKoin {
            androidContext(this@VelaNotesApp)
        }
    }
}
