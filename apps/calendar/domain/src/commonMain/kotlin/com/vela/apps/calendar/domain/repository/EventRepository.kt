/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.domain.repository

import com.vela.apps.calendar.domain.model.Event
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for calendar events. Implemented by the data layer (Room). */
interface EventRepository {
    /**
     * All events that *could* surface within `[startMillis, endMillis)`: either a non-recurring event
     * starting before the window's end, or any recurring master starting before the window's end (its
     * later repeats are computed in the domain). Recurrence expansion is performed by the caller.
     */
    fun observeInRange(startMillis: Long, endMillis: Long): Flow<List<Event>>

    /** All events, newest start first — used for search across the whole calendar. */
    fun observeAll(): Flow<List<Event>>

    fun observeEvent(id: Long): Flow<Event?>

    /** Inserts or updates; returns the event id. */
    suspend fun save(event: Event): Long

    /** Reads a single event once (null if it doesn't exist). */
    suspend fun getEvent(id: Long): Event?

    suspend fun delete(id: Long)
}
