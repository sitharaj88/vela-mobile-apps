/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {

    @Query("SELECT * FROM recordings ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<RecordingEntity>>

    @Query("SELECT filePath FROM recordings WHERE id = :id")
    suspend fun filePathFor(id: Long): String?

    @Insert
    suspend fun insert(recording: RecordingEntity): Long

    @Query("UPDATE recordings SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteById(id: Long)
}
