/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.data.di

import com.vela.apps.calendar.data.RoomEventRepository
import com.vela.apps.calendar.data.local.EventsDatabase
import com.vela.apps.calendar.data.local.buildEventsDatabase
import com.vela.apps.calendar.data.local.eventsPlatformDatabaseModule
import com.vela.apps.calendar.domain.repository.EventRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Wires the Calendar Room database, DAO, and repository binding. */
val eventsDataModule = module {
    includes(eventsPlatformDatabaseModule())
    single { buildEventsDatabase(get()) }
    single { get<EventsDatabase>().eventDao() }
    singleOf(::RoomEventRepository) bind EventRepository::class
}
