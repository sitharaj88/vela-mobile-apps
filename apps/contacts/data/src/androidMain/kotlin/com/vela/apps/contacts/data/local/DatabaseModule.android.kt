/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun contactsPlatformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<ContactsDatabase>> {
        val context = androidContext().applicationContext
        val dbFile = context.getDatabasePath(ContactsDatabase.FILE_NAME)
        Room.databaseBuilder<ContactsDatabase>(context, dbFile.absolutePath)
    }
}
