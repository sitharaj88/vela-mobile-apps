/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [AlarmEntity::class], version = 1, exportSchema = false)
@ConstructedBy(AlarmsDatabaseConstructor::class)
abstract class AlarmsDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        const val FILE_NAME = "clock_alarms.db"
    }
}

// Room's KSP processor generates the platform `actual` implementations of this constructor.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object AlarmsDatabaseConstructor : RoomDatabaseConstructor<AlarmsDatabase> {
    override fun initialize(): AlarmsDatabase
}
