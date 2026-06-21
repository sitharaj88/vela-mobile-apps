/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    /**
     * Candidates that may surface in `[start, end)`: non-recurring events overlapping the window, plus
     * every recurring master starting before the window's end (later repeats are expanded in domain).
     */
    @Query(
        """
        SELECT * FROM events
        WHERE (recurrence = 'NONE' AND endMillis > :start AND startMillis < :end)
           OR (recurrence != 'NONE' AND startMillis < :end)
        ORDER BY startMillis
        """,
    )
    fun observeInRange(start: Long, end: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY startMillis DESC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun observeById(id: Long): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Upsert
    suspend fun upsert(event: EventEntity): Long

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)
}
