/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY id DESC LIMIT :limit")
    fun observe(limit: Int): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(entity: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clear()

    // Keep history bounded: delete everything older than the newest [keep] rows.
    @Query("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY id DESC LIMIT :keep)")
    suspend fun trimTo(keep: Int)
}
