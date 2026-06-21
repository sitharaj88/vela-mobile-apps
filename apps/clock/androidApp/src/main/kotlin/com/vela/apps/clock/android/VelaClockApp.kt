/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.android

import android.app.Application
import com.vela.apps.clock.ui.di.initClockKoin
import com.vela.apps.clock.ui.di.rescheduleAlarms
import org.koin.android.ext.koin.androidContext

class VelaClockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initClockKoin {
            androidContext(this@VelaClockApp)
        }.rescheduleAlarms()
    }
}
