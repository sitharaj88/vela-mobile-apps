/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.android

import android.app.Application
import com.vela.apps.calendar.ui.di.initCalendarKoin
import org.koin.android.ext.koin.androidContext

class VelaCalendarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initCalendarKoin {
            androidContext(this@VelaCalendarApp)
        }
    }
}
