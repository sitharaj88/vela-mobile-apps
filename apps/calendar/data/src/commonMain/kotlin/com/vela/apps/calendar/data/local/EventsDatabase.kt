/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [EventEntity::class], version = 1, exportSchema = false)
@ConstructedBy(EventsDatabaseConstructor::class)
abstract class EventsDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        const val FILE_NAME = "events.db"
    }
}

// Room's KSP processor generates the platform `actual` implementations of this constructor.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object EventsDatabaseConstructor : RoomDatabaseConstructor<EventsDatabase> {
    override fun initialize(): EventsDatabase
}
