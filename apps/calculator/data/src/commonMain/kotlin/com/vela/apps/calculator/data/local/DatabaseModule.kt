/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vela.core.common.platformIoDispatcher
import org.koin.core.module.Module

/**
 * Each platform provides a [RoomDatabase.Builder] for [CalculatorDatabase] (Android needs a
 * Context; Desktop/iOS resolve a file path). The common code below finalizes the build with the
 * bundled SQLite driver so behavior is identical everywhere.
 */
expect fun platformDatabaseModule(): Module

/** Finalizes a platform-provided builder into a usable database. */
fun buildCalculatorDatabase(builder: RoomDatabase.Builder<CalculatorDatabase>): CalculatorDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(platformIoDispatcher())
        .build()
