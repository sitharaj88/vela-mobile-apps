/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.ui.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Starts Koin with the Recorder modules. Each platform entry point calls this once at launch. */
fun initRecorderKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(recorderModule)
}

/** No-arg convenience for Swift interop. */
fun doInitRecorderKoin() {
    initRecorderKoin()
}
