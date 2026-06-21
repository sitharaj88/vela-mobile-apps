/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.domain.repository

import com.vela.apps.voicerecorder.domain.model.Recording
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for recordings. Implemented by the data layer (Room). */
interface RecordingRepository {
    /** All recordings, newest first. */
    fun observeAll(): Flow<List<Recording>>

    suspend fun add(name: String, filePath: String, durationMs: Long, sizeBytes: Long)

    suspend fun rename(id: Long, name: String)

    suspend fun delete(id: Long)
}
