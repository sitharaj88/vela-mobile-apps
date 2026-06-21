/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual fun contactsPlatformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<ContactsDatabase>> {
        val appDir = File(System.getProperty("user.home"), ".vela").apply { mkdirs() }
        val dbFile = File(appDir, ContactsDatabase.FILE_NAME)
        Room.databaseBuilder<ContactsDatabase>(name = dbFile.absolutePath)
    }
}
