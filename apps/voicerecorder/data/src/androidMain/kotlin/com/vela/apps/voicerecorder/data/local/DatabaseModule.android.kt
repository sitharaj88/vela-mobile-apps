/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun recordingsPlatformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<RecordingsDatabase>> {
        val context = androidContext().applicationContext
        val dbFile = context.getDatabasePath(RecordingsDatabase.FILE_NAME)
        Room.databaseBuilder<RecordingsDatabase>(context, dbFile.absolutePath)
    }
}
