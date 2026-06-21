/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabaseConstructor
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(entities = [NoteEntity::class], version = 2, exportSchema = false)
@ConstructedBy(NotesDatabaseConstructor::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        const val FILE_NAME = "notes.db"

        /**
         * v1 -> v2: adds checklist/pin/archive/color/tags/created-at support. Existing rows become
         * plain pinned-off, unarchived TEXT notes with no checklist/tags; [createdAtEpochMs] is
         * back-filled from the note's last-updated time so created-sort stays sensible.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE notes ADD COLUMN createdAtEpochMs INTEGER NOT NULL DEFAULT 0",
                )
                connection.execSQL("ALTER TABLE notes ADD COLUMN type TEXT NOT NULL DEFAULT 'TEXT'")
                connection.execSQL("ALTER TABLE notes ADD COLUMN items TEXT NOT NULL DEFAULT ''")
                connection.execSQL("ALTER TABLE notes ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE notes ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE notes ADD COLUMN color TEXT NOT NULL DEFAULT 'None'")
                connection.execSQL("ALTER TABLE notes ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                connection.execSQL("UPDATE notes SET createdAtEpochMs = updatedAtEpochMs")
            }
        }
    }
}

// Room's KSP processor generates the platform `actual` implementations of this constructor.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object NotesDatabaseConstructor : RoomDatabaseConstructor<NotesDatabase> {
    override fun initialize(): NotesDatabase
}
