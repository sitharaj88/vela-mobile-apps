/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.domain.model

import kotlinx.datetime.Instant

/** A single persisted audio recording. [id] == 0 denotes a not-yet-persisted recording. */
data class Recording(
    val id: Long = 0,
    val name: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAt: Instant,
)
