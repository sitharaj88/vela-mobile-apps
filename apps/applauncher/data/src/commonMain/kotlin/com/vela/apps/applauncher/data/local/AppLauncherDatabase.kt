/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = false)
@ConstructedBy(AppLauncherDatabaseConstructor::class)
abstract class AppLauncherDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val FILE_NAME = "applauncher.db"
    }
}

// Room's KSP processor generates the platform `actual` implementations of this constructor.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object AppLauncherDatabaseConstructor : RoomDatabaseConstructor<AppLauncherDatabase> {
    override fun initialize(): AppLauncherDatabase
}
