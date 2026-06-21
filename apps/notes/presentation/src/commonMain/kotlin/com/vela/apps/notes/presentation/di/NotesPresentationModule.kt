/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.presentation.di

import com.vela.apps.notes.domain.usecase.DeleteNoteUseCase
import com.vela.apps.notes.domain.usecase.ObserveAllNotesUseCase
import com.vela.apps.notes.domain.usecase.ObserveNoteUseCase
import com.vela.apps.notes.domain.usecase.ObserveNotesUseCase
import com.vela.apps.notes.domain.usecase.SaveNoteUseCase
import com.vela.apps.notes.domain.usecase.SetArchivedUseCase
import com.vela.apps.notes.domain.usecase.SetPinnedUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Note use cases. Stores (ViewModels) are bound in the UI module. */
val notesPresentationModule = module {
    factoryOf(::ObserveNotesUseCase)
    factoryOf(::ObserveAllNotesUseCase)
    factoryOf(::ObserveNoteUseCase)
    factoryOf(::SaveNoteUseCase)
    factoryOf(::SetPinnedUseCase)
    factoryOf(::SetArchivedUseCase)
    factoryOf(::DeleteNoteUseCase)
}
