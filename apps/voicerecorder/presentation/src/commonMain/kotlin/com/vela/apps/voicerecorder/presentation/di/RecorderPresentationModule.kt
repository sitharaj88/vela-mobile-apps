/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.presentation.di

import com.vela.apps.voicerecorder.domain.usecase.AddRecordingUseCase
import com.vela.apps.voicerecorder.domain.usecase.DeleteRecordingUseCase
import com.vela.apps.voicerecorder.domain.usecase.ObserveRecordingsUseCase
import com.vela.apps.voicerecorder.domain.usecase.RenameRecordingUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Recorder use cases. Stores (ViewModels) are bound in the UI module. */
val recorderPresentationModule = module {
    factoryOf(::ObserveRecordingsUseCase)
    factoryOf(::AddRecordingUseCase)
    factoryOf(::RenameRecordingUseCase)
    factoryOf(::DeleteRecordingUseCase)
}
