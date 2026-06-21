/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room row for a calendar event. Times are epoch millis; [recurrence] stores [Recurrence.name]. */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val location: String,
    val startMillis: Long,
    val endMillis: Long,
    val allDay: Boolean,
    val colorIndex: Int,
    val recurrence: String,
    val reminderMinutes: Int?,
)
