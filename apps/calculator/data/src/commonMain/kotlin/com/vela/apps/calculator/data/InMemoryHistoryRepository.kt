/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 *
 * NOTE: This is the reference (in-memory) implementation that keeps the first app fully runnable
 * on all three platforms without the Room/KSP/native toolchain. The production implementation is
 * Room-backed (Phase: persistence). Because callers depend only on HistoryRepository, swapping is
 * a single DI binding change with no impact on domain/presentation/ui.
 */
package com.vela.apps.calculator.data

import com.vela.apps.calculator.domain.model.HistoryEntry
import com.vela.apps.calculator.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

class InMemoryHistoryRepository : HistoryRepository {

    private val entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    private val writeLock = Mutex()

    override fun observeHistory(): Flow<List<HistoryEntry>> = entries.asStateFlow()

    override suspend fun add(expression: String, result: String) = writeLock.withLock {
        val nextId = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val entry = HistoryEntry(
            id = nextId,
            expression = expression,
            result = result,
            timestamp = Clock.System.now(),
        )
        // Newest first; cap to a sane window so memory stays bounded.
        entries.update { (listOf(entry) + it).take(MAX_ENTRIES) }
    }

    override suspend fun clear() = writeLock.withLock {
        entries.update { emptyList() }
    }

    private companion object {
        const val MAX_ENTRIES = 100
    }
}
