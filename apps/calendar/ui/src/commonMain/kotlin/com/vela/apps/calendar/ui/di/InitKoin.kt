/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Starts Koin with the Calendar modules. Each platform entry point calls this once at launch. */
fun initCalendarKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(calendarModule)
}

/** No-arg convenience for Swift interop. */
fun doInitCalendarKoin() {
    initCalendarKoin()
}
