/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.domain.recurrence

import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.model.EventOccurrence
import com.vela.apps.calendar.domain.model.Recurrence
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Hard cap on generated occurrences for a single recurring master event within one expansion. Guards
 * against pathological ranges (e.g. a daily event expanded over decades). Roughly two years of daily.
 */
private const val MAX_OCCURRENCES = 750

/**
 * Expand [events] into concrete [EventOccurrence]s that intersect the half-open range
 * `[rangeStartMillis, rangeEndMillis)`, in the given [timeZone] (recurrence steps are calendar-aware:
 * monthly/yearly preserve day-of-month/month-of-year across varying month lengths and leap years).
 *
 * Pure and deterministic — the unit of truth tested in `commonTest`. An occurrence is included when
 * its own span overlaps the window. Results are sorted by start instant.
 */
fun expandOccurrences(
    events: List<Event>,
    rangeStartMillis: Long,
    rangeEndMillis: Long,
    timeZone: TimeZone,
): List<EventOccurrence> =
    events
        .flatMap { event -> expandEvent(event, rangeStartMillis, rangeEndMillis, timeZone) }
        .sortedBy { it.startMillis }

private fun expandEvent(
    event: Event,
    rangeStartMillis: Long,
    rangeEndMillis: Long,
    timeZone: TimeZone,
): List<EventOccurrence> {
    val durationMillis = (event.endMillis - event.startMillis).coerceAtLeast(0)
    if (event.recurrence == Recurrence.NONE) {
        return listOfNotNull(
            occurrenceIfInRange(event, event.startMillis, durationMillis, rangeStartMillis, rangeEndMillis),
        )
    }
    return expandRepeating(event, durationMillis, rangeStartMillis, rangeEndMillis, timeZone)
}

private fun expandRepeating(
    event: Event,
    durationMillis: Long,
    rangeStartMillis: Long,
    rangeEndMillis: Long,
    timeZone: TimeZone,
): List<EventOccurrence> {
    val result = mutableListOf<EventOccurrence>()
    var startInstant = Instant.fromEpochMilliseconds(event.startMillis)
    var guard = 0
    while (guard < MAX_OCCURRENCES) {
        val startMillis = startInstant.toEpochMilliseconds()
        // The master never starts after the window ends — once it does, all later repeats do too.
        if (startMillis >= rangeEndMillis) break
        occurrenceIfInRange(event, startMillis, durationMillis, rangeStartMillis, rangeEndMillis)
            ?.let(result::add)
        startInstant = nextStart(startInstant, event.recurrence, timeZone)
        guard++
    }
    return result
}

/** Advance one recurrence step from [start], staying calendar-correct via local-date arithmetic. */
private fun nextStart(start: Instant, recurrence: Recurrence, timeZone: TimeZone): Instant {
    val local = start.toLocalDateTime(timeZone)
    val nextDate = when (recurrence) {
        Recurrence.DAILY -> local.date.plus(1, DateTimeUnit.DAY)
        Recurrence.WEEKLY -> local.date.plus(1, DateTimeUnit.WEEK)
        Recurrence.MONTHLY -> local.date.plus(1, DateTimeUnit.MONTH)
        Recurrence.YEARLY -> local.date.plus(1, DateTimeUnit.YEAR)
        Recurrence.NONE -> return start
    }
    return nextDate.atTime(local.time).toInstant(timeZone)
}

private fun occurrenceIfInRange(
    event: Event,
    startMillis: Long,
    durationMillis: Long,
    rangeStartMillis: Long,
    rangeEndMillis: Long,
): EventOccurrence? {
    val endMillis = startMillis + durationMillis
    // Overlap with the half-open window [rangeStart, rangeEnd): end strictly after start, start before end.
    val overlaps = endMillis > rangeStartMillis && startMillis < rangeEndMillis
    return if (overlaps) EventOccurrence(event, startMillis, endMillis) else null
}
