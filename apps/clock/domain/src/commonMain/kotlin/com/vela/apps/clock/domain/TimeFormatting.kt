/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

private fun Int.pad2(): String = toString().padStart(2, '0')

private fun Long.pad2(): String = toString().padStart(2, '0')

private const val MILLIS_PER_CENTI = 10
private const val CENTIS_PER_SECOND = 100
private const val SECONDS_PER_MINUTE = 60
private const val MINUTES_PER_HOUR = 60
private const val SECONDS_PER_HOUR = 3600

/** Stopwatch format: `mm:ss.cs`, widening to `h:mm:ss.cs` past an hour. */
fun formatStopwatch(elapsed: Duration): String {
    val totalCentis = elapsed.inWholeMilliseconds / MILLIS_PER_CENTI
    val centis = totalCentis % CENTIS_PER_SECOND
    val totalSeconds = totalCentis / CENTIS_PER_SECOND
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    val minutes = (totalSeconds / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR
    val hours = totalSeconds / SECONDS_PER_HOUR
    return if (hours > 0) {
        "$hours:${minutes.pad2()}:${seconds.pad2()}.${centis.pad2()}"
    } else {
        "${minutes.pad2()}:${seconds.pad2()}.${centis.pad2()}"
    }
}

/** Countdown format: `mm:ss`, widening to `h:mm:ss` past an hour. Rounds up so 0.4s still shows 00:01. */
fun formatTimer(remaining: Duration): String {
    val totalSeconds = (remaining.inWholeMilliseconds + 999) / 1000
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    val minutes = (totalSeconds / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR
    val hours = totalSeconds / SECONDS_PER_HOUR
    return if (hours > 0) {
        "$hours:${minutes.pad2()}:${seconds.pad2()}"
    } else {
        "${minutes.pad2()}:${seconds.pad2()}"
    }
}

/** Current wall-clock time + a short date label, in the given [timeZone]. */
fun formatClock(instant: Instant, timeZone: TimeZone): ClockDisplay {
    val dt = instant.toLocalDateTime(timeZone)
    val time = "${dt.hour.pad2()}:${dt.minute.pad2()}:${dt.second.pad2()}"
    val weekday = dt.dayOfWeek.name.titleCase().take(3)
    val month = dt.month.name.titleCase().take(3)
    return ClockDisplay(time = time, date = "$weekday, $month ${dt.dayOfMonth}")
}

private fun String.titleCase(): String =
    lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
