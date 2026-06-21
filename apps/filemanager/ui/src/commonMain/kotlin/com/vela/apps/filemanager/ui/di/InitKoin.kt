/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.ui.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Starts Koin with the File Manager modules. Each platform entry point calls this once at launch. */
fun initFileManagerKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(fileManagerModule)
}

/** No-arg convenience for Swift interop. */
fun doInitFileManagerKoin() {
    initFileManagerKoin()
}
