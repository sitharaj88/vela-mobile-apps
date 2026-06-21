/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.ui.di

import com.vela.apps.voicerecorder.data.di.recorderDataModule
import com.vela.apps.voicerecorder.presentation.RecorderStore
import com.vela.apps.voicerecorder.presentation.di.recorderPresentationModule
import com.vela.core.common.DefaultDispatcherProvider
import com.vela.core.common.DispatcherProvider
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole Recorder app. */
val recorderModule = module {
    includes(recorderDataModule, recorderPresentationModule)
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    viewModelOf(::RecorderStore)
}
