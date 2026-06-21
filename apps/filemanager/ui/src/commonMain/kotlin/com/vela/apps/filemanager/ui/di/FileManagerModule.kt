/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.ui.di

import com.vela.apps.filemanager.data.fileSystemPlatformModule
import com.vela.apps.filemanager.presentation.FileBrowserStore
import com.vela.core.common.DefaultDispatcherProvider
import com.vela.core.common.DispatcherProvider
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole File Manager app. */
val fileManagerModule = module {
    includes(fileSystemPlatformModule())
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    viewModelOf(::FileBrowserStore)
}
