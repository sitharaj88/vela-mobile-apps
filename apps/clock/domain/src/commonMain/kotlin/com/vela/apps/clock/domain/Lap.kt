/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.domain

/** A recorded stopwatch lap. [split] is time since the previous lap; [total] is overall elapsed. */
data class Lap(
    val index: Int,
    val split: String,
    val total: String,
)

/** Formatted current-time display. */
data class ClockDisplay(
    val time: String,
    val date: String,
)
