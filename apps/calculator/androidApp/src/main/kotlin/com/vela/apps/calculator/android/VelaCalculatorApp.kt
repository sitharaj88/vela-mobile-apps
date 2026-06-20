/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.android

import android.app.Application
import com.vela.apps.calculator.ui.di.initCalculatorKoin
import org.koin.android.ext.koin.androidContext

class VelaCalculatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initCalculatorKoin {
            androidContext(this@VelaCalculatorApp)
        }
    }
}
