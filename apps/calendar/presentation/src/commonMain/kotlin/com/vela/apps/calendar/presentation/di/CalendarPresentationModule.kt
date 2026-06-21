/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation.di

import com.vela.apps.calendar.domain.usecase.DeleteEventUseCase
import com.vela.apps.calendar.domain.usecase.ObserveAllEventsUseCase
import com.vela.apps.calendar.domain.usecase.ObserveEventUseCase
import com.vela.apps.calendar.domain.usecase.ObserveOccurrencesUseCase
import com.vela.apps.calendar.domain.usecase.SaveEventUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Calendar use cases. Stores (ViewModels) are bound in the UI module. */
val calendarPresentationModule = module {
    factoryOf(::ObserveOccurrencesUseCase)
    factoryOf(::ObserveAllEventsUseCase)
    factoryOf(::ObserveEventUseCase)
    factoryOf(::SaveEventUseCase)
    factoryOf(::DeleteEventUseCase)
}
