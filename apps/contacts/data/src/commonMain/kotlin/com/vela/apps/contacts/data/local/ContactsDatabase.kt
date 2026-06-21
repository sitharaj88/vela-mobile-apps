/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [ContactEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(ContactsDatabaseConstructor::class)
abstract class ContactsDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val FILE_NAME = "contacts.db"
    }
}

// Room's KSP processor generates the platform `actual` implementations of this constructor.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object ContactsDatabaseConstructor : RoomDatabaseConstructor<ContactsDatabase> {
    override fun initialize(): ContactsDatabase
}
