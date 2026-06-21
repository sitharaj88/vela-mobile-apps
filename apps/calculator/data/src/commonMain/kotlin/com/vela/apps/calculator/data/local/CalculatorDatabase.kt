/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

// exportSchema disabled for v1 (single schema, no migrations yet). Enable + wire room.schemaLocation
// per target when the first migration lands.
@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
@ConstructedBy(CalculatorDatabaseConstructor::class)
abstract class CalculatorDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        const val FILE_NAME = "calculator.db"
    }
}

// Room's KSP processor generates the platform `actual` implementations of this constructor.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object CalculatorDatabaseConstructor : RoomDatabaseConstructor<CalculatorDatabase> {
    override fun initialize(): CalculatorDatabase
}
