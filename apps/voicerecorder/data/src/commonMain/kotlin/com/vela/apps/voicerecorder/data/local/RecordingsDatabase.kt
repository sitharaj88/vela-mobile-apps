/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [RecordingEntity::class], version = 1, exportSchema = false)
@ConstructedBy(RecordingsDatabaseConstructor::class)
abstract class RecordingsDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao

    companion object {
        const val FILE_NAME = "recordings.db"
    }
}

// Room's KSP processor generates the platform `actual` implementations of this constructor.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object RecordingsDatabaseConstructor : RoomDatabaseConstructor<RecordingsDatabase> {
    override fun initialize(): RecordingsDatabase
}
