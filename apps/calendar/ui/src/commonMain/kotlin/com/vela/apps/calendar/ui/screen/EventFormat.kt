/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.screen

import com.vela.apps.calendar.domain.model.EventOccurrence
import com.vela.apps.calendar.presentation.month.localDateTimeOf
import com.vela.apps.calendar.presentation.month.monthLabelShort
import com.vela.apps.calendar.presentation.month.weekdayLabelShort
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** `HH:mm`, KMP-safe (no String.format). */
fun formatTime(time: LocalTime): String =
    "${time.hour.pad2()}:${time.minute.pad2()}"

/** Full date header, e.g. `Mon, Jun 21 2026`. */
fun formatDateFull(date: LocalDate): String =
    "${weekdayLabelShort(date)}, ${monthLabelShort(date)} ${date.dayOfMonth} ${date.year}"

/** Short date, e.g. `Jun 21`. */
fun formatDateShort(date: LocalDate): String =
    "${monthLabelShort(date)} ${date.dayOfMonth}"

/** The time-range label for an occurrence, e.g. `09:00 – 10:00`, or `All day`. */
fun occurrenceTimeLabel(occurrence: EventOccurrence): String {
    if (occurrence.event.allDay) return "All day"
    val start = localDateTimeOf(occurrence.startMillis)
    val end = localDateTimeOf(occurrence.endMillis)
    return "${formatTime(start.time)} – ${formatTime(end.time)}"
}

/** Reminder label for the editor chips, e.g. `None`, `At start`, `15 min before`, `1 day before`. */
fun reminderLabel(minutes: Int?): String = when (minutes) {
    null -> "None"
    0 -> "At start"
    MINUTES_PER_HOUR -> "1 hour before"
    MINUTES_PER_DAY -> "1 day before"
    else -> if (minutes % MINUTES_PER_HOUR == 0) {
        "${minutes / MINUTES_PER_HOUR} hr before"
    } else {
        "$minutes min before"
    }
}

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 1440

private fun Int.pad2(): String = toString().padStart(2, '0')
