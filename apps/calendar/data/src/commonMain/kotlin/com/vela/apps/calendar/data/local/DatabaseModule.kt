/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vela.core.common.platformIoDispatcher
import org.koin.core.module.Module

/** Each platform provides a [RoomDatabase.Builder]; common code finalizes it identically. */
expect fun eventsPlatformDatabaseModule(): Module

fun buildEventsDatabase(builder: RoomDatabase.Builder<EventsDatabase>): EventsDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(platformIoDispatcher())
        .build()
