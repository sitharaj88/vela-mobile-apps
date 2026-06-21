/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row for an alarm. [repeatDaysCsv] stores the repeat schedule as comma-separated [WeekDay]
 * ordinals (e.g. "0,2,4"); empty string means a one-shot alarm.
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val enabled: Boolean,
    val repeatDaysCsv: String,
)
