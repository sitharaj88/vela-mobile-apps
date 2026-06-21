/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.ui.screen

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

private const val MILLIS_PER_SECOND = 1000L
private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60L
private const val HOURS_PER_DAY = 24L

/** Coarse relative time ("just now", "5 min ago", "3 h ago", "2 d ago"). */
internal fun relativeTime(createdAt: Instant, now: Instant = Clock.System.now()): String {
    val deltaMs = (now - createdAt).inWholeMilliseconds.coerceAtLeast(0)
    val seconds = deltaMs / MILLIS_PER_SECOND
    val minutes = seconds / SECONDS_PER_MINUTE
    val hours = minutes / MINUTES_PER_HOUR
    val days = hours / HOURS_PER_DAY
    return when {
        minutes < 1 -> "just now"
        minutes < MINUTES_PER_HOUR -> "$minutes min ago"
        hours < HOURS_PER_DAY -> "$hours h ago"
        else -> "$days d ago"
    }
}
