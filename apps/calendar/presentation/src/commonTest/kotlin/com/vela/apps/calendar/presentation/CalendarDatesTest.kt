/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation

import com.vela.apps.calendar.presentation.month.CalendarView
import com.vela.apps.calendar.presentation.month.monthGrid
import com.vela.apps.calendar.presentation.month.startOfDayMillis
import com.vela.apps.calendar.presentation.month.visibleRange
import com.vela.apps.calendar.presentation.month.weekDays
import com.vela.apps.calendar.presentation.month.weekOfYear
import com.vela.apps.calendar.presentation.month.weekStart
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarDatesTest {

    private val utc = TimeZone.UTC

    @Test
    fun monthGrid_is_six_weeks_of_seven_monday_first_days() {
        val grid = monthGrid(2026, 6)
        assertEquals(6, grid.size)
        grid.forEach { assertEquals(7, it.size) }
        // Every first column is a Monday.
        grid.forEach { assertEquals(DayOfWeek.MONDAY, it.first().dayOfWeek) }
    }

    @Test
    fun monthGrid_pads_with_adjacent_month_days() {
        // June 2026: the 1st is a Monday, so the grid starts exactly on June 1.
        val grid = monthGrid(2026, 6)
        assertEquals(LocalDate(2026, 6, 1), grid.first().first())
        // July 2026: the 1st is a Wednesday, so the grid starts on Mon June 29.
        val july = monthGrid(2026, 7)
        assertEquals(LocalDate(2026, 6, 29), july.first().first())
    }

    @Test
    fun weekStart_is_the_monday_on_or_before() {
        assertEquals(LocalDate(2026, 6, 15), weekStart(LocalDate(2026, 6, 15))) // Monday
        assertEquals(LocalDate(2026, 6, 15), weekStart(LocalDate(2026, 6, 21))) // Sunday -> prior Monday
    }

    @Test
    fun weekDays_returns_seven_consecutive_days_from_monday() {
        val days = weekDays(LocalDate(2026, 6, 18))
        assertEquals(7, days.size)
        assertEquals(LocalDate(2026, 6, 15), days.first())
        assertEquals(LocalDate(2026, 6, 21), days.last())
    }

    @Test
    fun weekOfYear_matches_iso_week_numbers() {
        // ISO week 1 of 2026 contains Jan 1 (Thursday).
        assertEquals(1, weekOfYear(LocalDate(2026, 1, 1)))
        // 2025-12-29 (Monday) belongs to ISO week 1 of 2026.
        assertEquals(1, weekOfYear(LocalDate(2025, 12, 29)))
        assertEquals(25, weekOfYear(LocalDate(2026, 6, 15)))
    }

    @Test
    fun visibleRange_day_covers_exactly_one_day() {
        val anchor = LocalDate(2026, 6, 15)
        val range = visibleRange(CalendarView.DAY, anchor, utc)
        assertEquals(startOfDayMillis(anchor, utc), range.startMillis)
        assertEquals(startOfDayMillis(LocalDate(2026, 6, 16), utc), range.endMillis)
        assertEquals(anchor, range.firstDay)
    }

    @Test
    fun visibleRange_week_covers_monday_to_next_monday() {
        val range = visibleRange(CalendarView.WEEK, LocalDate(2026, 6, 18), utc)
        assertEquals(startOfDayMillis(LocalDate(2026, 6, 15), utc), range.startMillis)
        assertEquals(startOfDayMillis(LocalDate(2026, 6, 22), utc), range.endMillis)
    }

    @Test
    fun visibleRange_month_spans_the_full_grid() {
        val grid = monthGrid(2026, 6)
        val range = visibleRange(CalendarView.MONTH, LocalDate(2026, 6, 15), utc)
        assertEquals(startOfDayMillis(grid.first().first(), utc), range.startMillis)
    }

    @Test
    fun visibleRange_agenda_spans_thirty_days() {
        val anchor = LocalDate(2026, 6, 1)
        val range = visibleRange(CalendarView.AGENDA, anchor, utc)
        assertEquals(startOfDayMillis(anchor, utc), range.startMillis)
        // 30 days -> ends at start of July 1.
        assertEquals(startOfDayMillis(LocalDate(2026, 7, 1), utc), range.endMillis)
    }
}
