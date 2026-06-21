/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data

import com.vela.apps.voicerecorder.data.io.deleteRecordingFile
import com.vela.apps.voicerecorder.data.local.RecordingDao
import com.vela.apps.voicerecorder.data.local.RecordingEntity
import com.vela.apps.voicerecorder.domain.model.Recording
import com.vela.apps.voicerecorder.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/** Room-backed [RecordingRepository]; maps between the Room entity and the domain model. */
class RoomRecordingRepository(
    private val dao: RecordingDao,
) : RecordingRepository {

    override fun observeAll(): Flow<List<Recording>> =
        dao.observeAll().map { rows -> rows.map(RecordingEntity::toDomain) }

    override suspend fun add(name: String, filePath: String, durationMs: Long, sizeBytes: Long) {
        dao.insert(
            RecordingEntity(
                name = name,
                filePath = filePath,
                durationMs = durationMs,
                sizeBytes = sizeBytes,
                createdAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            ),
        )
    }

    override suspend fun rename(id: Long, name: String) = dao.rename(id, name)

    override suspend fun delete(id: Long) {
        val path = dao.filePathFor(id)
        dao.deleteById(id)
        if (path != null) deleteRecordingFile(path)
    }
}

private fun RecordingEntity.toDomain() = Recording(
    id = id,
    name = name,
    filePath = filePath,
    durationMs = durationMs,
    sizeBytes = sizeBytes,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs),
)
