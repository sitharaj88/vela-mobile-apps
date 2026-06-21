/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun appLauncherPlatformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<AppLauncherDatabase>> {
        val context = androidContext().applicationContext
        val dbFile = context.getDatabasePath(AppLauncherDatabase.FILE_NAME)
        Room.databaseBuilder<AppLauncherDatabase>(context, dbFile.absolutePath)
    }
}
