/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.domain.model

import kotlinx.datetime.Instant

/** A single completed calculation, persisted to history. */
data class HistoryEntry(
    val id: Long,
    val expression: String,
    val result: String,
    val timestamp: Instant,
)
