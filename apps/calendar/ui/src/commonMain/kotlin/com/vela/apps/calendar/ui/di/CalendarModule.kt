/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.di

import com.vela.apps.calendar.data.di.eventsDataModule
import com.vela.apps.calendar.presentation.di.calendarPresentationModule
import com.vela.apps.calendar.presentation.editor.EventEditorStore
import com.vela.apps.calendar.presentation.month.CalendarStore
import com.vela.apps.calendar.ui.platform.calendarPlatformModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole Calendar app. */
val calendarModule = module {
    includes(eventsDataModule, calendarPresentationModule, calendarPlatformModule())
    // Explicit constructors so the stores' defaulted `zone` parameter isn't resolved from Koin.
    viewModel { CalendarStore(get(), get()) }
    // The editor receives its event id (0 = new) and seed epoch-day as runtime parameters.
    viewModel { (eventId: Long, epochDay: Long) ->
        EventEditorStore(eventId, epochDay, get(), get(), get())
    }
}
