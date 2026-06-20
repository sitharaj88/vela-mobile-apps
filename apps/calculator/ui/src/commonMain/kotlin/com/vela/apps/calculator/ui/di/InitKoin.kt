/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.ui.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Starts Koin with the Calculator's modules. Each platform entry point calls this once at launch
 * (Android: Application; Desktop: main(); iOS: from Swift before loading the Compose controller).
 */
fun initCalculatorKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(calculatorModule)
}

/** No-arg convenience for Swift interop (default lambda params don't bridge cleanly to ObjC). */
fun doInitCalculatorKoin() {
    initCalculatorKoin()
}
