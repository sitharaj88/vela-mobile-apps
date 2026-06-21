/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.domain.usecase

import com.vela.apps.voicerecorder.domain.model.Recording
import com.vela.apps.voicerecorder.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow

/** Observe all recordings, newest first. */
class ObserveRecordingsUseCase(private val repository: RecordingRepository) {
    operator fun invoke(): Flow<List<Recording>> = repository.observeAll()
}

/** Persist a freshly captured recording. */
class AddRecordingUseCase(private val repository: RecordingRepository) {
    suspend operator fun invoke(name: String, filePath: String, durationMs: Long, sizeBytes: Long) =
        repository.add(name, filePath, durationMs, sizeBytes)
}

/** Rename an existing recording (blank names are ignored, trimmed otherwise). */
class RenameRecordingUseCase(private val repository: RecordingRepository) {
    suspend operator fun invoke(id: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) repository.rename(id, trimmed)
    }
}

class DeleteRecordingUseCase(private val repository: RecordingRepository) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
