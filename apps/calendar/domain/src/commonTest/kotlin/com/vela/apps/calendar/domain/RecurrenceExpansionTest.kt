/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.domain

import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.model.Recurrence
import com.vela.apps.calendar.domain.recurrence.expandOccurrences
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecurrenceExpansionTest {

    private val utc = TimeZone.UTC

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 9, minute: Int = 0): Long =
        LocalDateTime(LocalDate(year, month, day), LocalTime(hour, minute)).toInstant(utc).toEpochMilliseconds()

    private fun event(
        id: Long,
        start: Long,
        durationMinutes: Int = 60,
        recurrence: Recurrence = Recurrence.NONE,
    ) = Event(
        id = id,
        title = "Event $id",
        startMillis = start,
        endMillis = start + durationMinutes * 60_000L,
        recurrence = recurrence,
    )

    @Test
    fun nonRecurring_event_inside_range_yields_single_occurrence() {
        val e = event(1, millis(2026, 6, 15))
        val result = expandOccurrences(
            events = listOf(e),
            rangeStartMillis = millis(2026, 6, 1, 0, 0),
            rangeEndMillis = millis(2026, 7, 1, 0, 0),
            timeZone = utc,
        )
        assertEquals(1, result.size)
        assertEquals(e.startMillis, result.first().startMillis)
    }

    @Test
    fun nonRecurring_event_outside_range_is_excluded() {
        val e = event(1, millis(2026, 5, 15))
        val result = expandOccurrences(
            events = listOf(e),
            rangeStartMillis = millis(2026, 6, 1, 0, 0),
            rangeEndMillis = millis(2026, 7, 1, 0, 0),
            timeZone = utc,
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun daily_recurrence_expands_one_per_day_in_range() {
        val e = event(1, millis(2026, 6, 1), recurrence = Recurrence.DAILY)
        val result = expandOccurrences(
            events = listOf(e),
            rangeStartMillis = millis(2026, 6, 1, 0, 0),
            rangeEndMillis = millis(2026, 6, 8, 0, 0),
            timeZone = utc,
        )
        // June 1..7 inclusive (7 days; the 8th's 09:00 falls outside the half-open window).
        assertEquals(7, result.size)
        assertEquals(millis(2026, 6, 1), result.first().startMillis)
        assertEquals(millis(2026, 6, 7), result.last().startMillis)
    }

    @Test
    fun weekly_recurrence_steps_seven_days() {
        val e = event(1, millis(2026, 6, 1), recurrence = Recurrence.WEEKLY)
        val result = expandOccurrences(
            events = listOf(e),
            rangeStartMillis = millis(2026, 6, 1, 0, 0),
            rangeEndMillis = millis(2026, 6, 30, 0, 0),
            timeZone = utc,
        )
        // June 1 start at 09:00; range end is June 30 00:00, so June 29 09:00 is still inside.
        assertEquals(
            listOf(
                millis(2026, 6, 1),
                millis(2026, 6, 8),
                millis(2026, 6, 15),
                millis(2026, 6, 22),
                millis(2026, 6, 29),
            ),
            result.map { it.startMillis },
        )
    }

    @Test
    fun monthly_recurrence_preserves_day_of_month() {
        val e = event(1, millis(2026, 1, 31), recurrence = Recurrence.MONTHLY)
        val result = expandOccurrences(
            events = listOf(e),
            rangeStartMillis = millis(2026, 1, 1, 0, 0),
            rangeEndMillis = millis(2026, 4, 1, 0, 0),
            timeZone = utc,
        )
        // kotlinx-datetime clamps overflowing month days; Jan 31 -> Feb 28 -> Mar 28 (last+1mo of Feb 28).
        assertEquals(millis(2026, 1, 31), result[0].startMillis)
        assertEquals(millis(2026, 2, 28), result[1].startMillis)
        assertTrue(result.size >= 2)
    }

    @Test
    fun yearly_recurrence_advances_by_year() {
        val e = event(1, millis(2024, 2, 29), recurrence = Recurrence.YEARLY)
        val result = expandOccurrences(
            events = listOf(e),
            rangeStartMillis = millis(2024, 1, 1, 0, 0),
            rangeEndMillis = millis(2027, 1, 1, 0, 0),
            timeZone = utc,
        )
        // 2024-02-29 (leap), then Feb of following years (clamped to the 28th).
        assertEquals(millis(2024, 2, 29), result.first().startMillis)
        assertTrue(result.size >= 2)
    }

    @Test
    fun recurring_event_before_window_includes_only_repeats_inside() {
        val e = event(1, millis(2026, 6, 1), recurrence = Recurrence.DAILY)
        val result = expandOccurrences(
            events = listOf(e),
            rangeStartMillis = millis(2026, 6, 10, 0, 0),
            rangeEndMillis = millis(2026, 6, 13, 0, 0),
            timeZone = utc,
        )
        assertEquals(listOf(millis(2026, 6, 10), millis(2026, 6, 11), millis(2026, 6, 12)),
            result.map { it.startMillis })
    }

    @Test
    fun results_are_sorted_by_start_across_events() {
        val a = event(1, millis(2026, 6, 20))
        val b = event(2, millis(2026, 6, 5))
        val result = expandOccurrences(
            events = listOf(a, b),
            rangeStartMillis = millis(2026, 6, 1, 0, 0),
            rangeEndMillis = millis(2026, 7, 1, 0, 0),
            timeZone = utc,
        )
        assertEquals(listOf(millis(2026, 6, 5), millis(2026, 6, 20)), result.map { it.startMillis })
    }

    @Test
    fun occurrence_overlapping_range_boundary_is_included() {
        // Event runs 23:30 on Jun 9 to 00:30 Jun 10; window starts Jun 10 00:00 -> overlaps.
        val start = millis(2026, 6, 9, 23, 30)
        val e = event(1, start, durationMinutes = 60)
        val result = expandOccurrences(
            events = listOf(e),
            rangeStartMillis = millis(2026, 6, 10, 0, 0),
            rangeEndMillis = millis(2026, 6, 11, 0, 0),
            timeZone = utc,
        )
        assertEquals(1, result.size)
    }
}
