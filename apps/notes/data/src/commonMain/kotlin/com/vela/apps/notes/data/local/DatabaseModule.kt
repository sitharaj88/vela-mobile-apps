/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vela.core.common.platformIoDispatcher
import org.koin.core.module.Module

/** Each platform provides a [RoomDatabase.Builder]; common code finalizes it identically. */
expect fun notesPlatformDatabaseModule(): Module

fun buildNotesDatabase(builder: RoomDatabase.Builder<NotesDatabase>): NotesDatabase =
    builder
        .addMigrations(NotesDatabase.MIGRATION_1_2)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(platformIoDispatcher())
        .build()
