/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vela.core.common.platformIoDispatcher
import org.koin.core.module.Module

/** Each platform provides a [RoomDatabase.Builder]; common code finalizes it identically. */
expect fun alarmsPlatformDatabaseModule(): Module

fun buildAlarmsDatabase(builder: RoomDatabase.Builder<AlarmsDatabase>): AlarmsDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(platformIoDispatcher())
        .build()
