/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.android

import android.app.Application
import com.vela.apps.gallery.ui.di.initGalleryKoin
import org.koin.android.ext.koin.androidContext

class VelaGalleryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initGalleryKoin {
            androidContext(this@VelaGalleryApp)
        }
    }
}
