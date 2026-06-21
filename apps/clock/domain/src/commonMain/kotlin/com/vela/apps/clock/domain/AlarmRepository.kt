/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.domain

import kotlinx.coroutines.flow.Flow

/** Persistence boundary for alarms. Implemented by the data layer (Room). */
interface AlarmRepository {
    /** All alarms, earliest time-of-day first. */
    fun observeAlarms(): Flow<List<Alarm>>

    /** Inserts or updates; returns the alarm id. */
    suspend fun save(alarm: Alarm): Long

    /** Toggles the enabled flag for [alarmId]. */
    suspend fun setEnabled(alarmId: Long, enabled: Boolean)

    suspend fun delete(alarmId: Long)
}
