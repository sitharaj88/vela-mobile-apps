/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.domain.repository

import com.vela.apps.calculator.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Persistence boundary for calculation history. The domain depends only on this interface; the
 * data layer provides the implementation (in-memory today, Room in production — swapping is a
 * one-line DI change because nothing above this layer knows the difference).
 */
interface HistoryRepository {
    fun observeHistory(): Flow<List<HistoryEntry>>
    suspend fun add(expression: String, result: String)
    suspend fun clear()
}
