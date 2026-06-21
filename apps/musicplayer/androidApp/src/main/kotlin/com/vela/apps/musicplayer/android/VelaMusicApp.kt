/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.android

import android.app.Application
import com.vela.apps.musicplayer.ui.di.initMusicKoin
import org.koin.android.ext.koin.androidContext

class VelaMusicApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initMusicKoin {
            androidContext(this@VelaMusicApp)
        }
    }
}
