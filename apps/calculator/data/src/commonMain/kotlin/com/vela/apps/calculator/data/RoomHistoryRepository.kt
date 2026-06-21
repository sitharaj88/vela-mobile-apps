/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.data

import com.vela.apps.calculator.data.local.HistoryDao
import com.vela.apps.calculator.data.local.HistoryEntity
import com.vela.apps.calculator.domain.model.HistoryEntry
import com.vela.apps.calculator.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/** Production [HistoryRepository] backed by Room. Maps between the Room entity and the domain model. */
class RoomHistoryRepository(
    private val dao: HistoryDao,
) : HistoryRepository {

    override fun observeHistory(): Flow<List<HistoryEntry>> =
        dao.observe(MAX_ENTRIES).map { rows -> rows.map(HistoryEntity::toDomain) }

    override suspend fun add(expression: String, result: String) {
        dao.insert(
            HistoryEntity(
                expression = expression,
                result = result,
                timestampEpochMs = Clock.System.now().toEpochMilliseconds(),
            ),
        )
        dao.trimTo(MAX_ENTRIES)
    }

    override suspend fun clear() = dao.clear()

    private companion object {
        const val MAX_ENTRIES = 100
    }
}

private fun HistoryEntity.toDomain() = HistoryEntry(
    id = id,
    expression = expression,
    result = result,
    timestamp = Instant.fromEpochMilliseconds(timestampEpochMs),
)
