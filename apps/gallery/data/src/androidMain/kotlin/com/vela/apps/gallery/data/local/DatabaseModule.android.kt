/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun galleryPlatformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<GalleryDatabase>> {
        val context = androidContext().applicationContext
        val dbFile = context.getDatabasePath(GalleryDatabase.FILE_NAME)
        Room.databaseBuilder<GalleryDatabase>(context, dbFile.absolutePath)
    }
}
