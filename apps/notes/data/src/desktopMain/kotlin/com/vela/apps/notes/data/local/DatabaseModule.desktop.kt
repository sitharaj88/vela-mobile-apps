/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual fun notesPlatformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<NotesDatabase>> {
        val appDir = File(System.getProperty("user.home"), ".vela").apply { mkdirs() }
        val dbFile = File(appDir, NotesDatabase.FILE_NAME)
        Room.databaseBuilder<NotesDatabase>(name = dbFile.absolutePath)
    }
}
