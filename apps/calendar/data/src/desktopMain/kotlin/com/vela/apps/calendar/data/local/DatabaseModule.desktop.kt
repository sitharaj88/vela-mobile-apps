/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual fun eventsPlatformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<EventsDatabase>> {
        val appDir = File(System.getProperty("user.home"), ".vela").apply { mkdirs() }
        val dbFile = File(appDir, EventsDatabase.FILE_NAME)
        Room.databaseBuilder<EventsDatabase>(name = dbFile.absolutePath)
    }
}
