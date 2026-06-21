/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.android

import android.app.Application
import com.vela.apps.contacts.ui.di.initContactsKoin
import org.koin.android.ext.koin.androidContext

class VelaContactsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initContactsKoin {
            androidContext(this@VelaContactsApp)
        }
    }
}
