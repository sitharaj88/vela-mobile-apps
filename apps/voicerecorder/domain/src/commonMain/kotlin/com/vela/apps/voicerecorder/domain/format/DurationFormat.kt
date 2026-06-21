/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.domain.format

private const val MILLIS_PER_SECOND = 1000L
private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60L
private const val BYTES_PER_KB = 1024.0
private const val KB_THRESHOLD = 1024L
private const val MB_THRESHOLD = 1024L * 1024L

/**
 * Formats a duration in millis as `m:ss` (or `h:mm:ss` for clips an hour or longer).
 * No `String.format` — JVM-only and banned in commonMain; padding is manual.
 */
fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0) / MILLIS_PER_SECOND
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    val totalMinutes = totalSeconds / SECONDS_PER_MINUTE
    val minutes = totalMinutes % MINUTES_PER_HOUR
    val hours = totalMinutes / MINUTES_PER_HOUR
    val ss = seconds.toString().padStart(2, '0')
    return if (hours > 0) {
        val mm = minutes.toString().padStart(2, '0')
        "$hours:$mm:$ss"
    } else {
        "$minutes:$ss"
    }
}

/** Human-readable file size: bytes / KB / MB with one decimal place (manual, no String.format). */
fun formatSize(sizeBytes: Long): String {
    val bytes = sizeBytes.coerceAtLeast(0)
    return when {
        bytes < KB_THRESHOLD -> "$bytes B"
        bytes < MB_THRESHOLD -> oneDecimal(bytes / BYTES_PER_KB) + " KB"
        else -> oneDecimal(bytes / BYTES_PER_KB / BYTES_PER_KB) + " MB"
    }
}

/** Rounds to one decimal place and renders as `n.d` without locale-sensitive String.format. */
private fun oneDecimal(value: Double): String {
    val scaled = (value * 10).toLong()
    val whole = scaled / 10
    val frac = scaled % 10
    return "$whole.$frac"
}
