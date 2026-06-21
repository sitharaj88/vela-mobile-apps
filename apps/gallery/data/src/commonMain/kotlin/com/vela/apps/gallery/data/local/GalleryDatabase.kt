/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = false)
@ConstructedBy(GalleryDatabaseConstructor::class)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val FILE_NAME = "gallery.db"
    }
}

// Room's KSP processor generates the platform `actual` implementations of this constructor.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object GalleryDatabaseConstructor : RoomDatabaseConstructor<GalleryDatabase> {
    override fun initialize(): GalleryDatabase
}
