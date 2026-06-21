/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation.month

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/** Switchable calendar views. */
enum class CalendarView {
    MONTH,
    WEEK,
    DAY,
    AGENDA,
}

/** A half-open visible millis range `[startMillis, endMillis)` plus the first day it covers. */
data class VisibleRange(
    val startMillis: Long,
    val endMillis: Long,
    val firstDay: LocalDate,
)

private const val AGENDA_DAYS = 30

/**
 * The half-open millis range a given [view] observes when anchored at [anchor]. Month covers the full
 * 6x7 grid (so dots line up with the month screen); Week covers Mon..Sun; Day covers one day; Agenda
 * covers [AGENDA_DAYS] days starting at [anchor]. Pure and tested.
 */
fun visibleRange(
    view: CalendarView,
    anchor: LocalDate,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): VisibleRange = when (view) {
    CalendarView.MONTH -> {
        val grid = monthGrid(anchor.year, anchor.monthNumber)
        val first = grid.first().first()
        val lastExclusive = grid.last().last()
        VisibleRange(startOfDayMillis(first, zone), endOfDayExclusiveMillis(lastExclusive, zone), first)
    }
    CalendarView.WEEK -> {
        val days = weekDays(anchor)
        VisibleRange(
            startOfDayMillis(days.first(), zone),
            endOfDayExclusiveMillis(days.last(), zone),
            days.first(),
        )
    }
    CalendarView.DAY ->
        VisibleRange(startOfDayMillis(anchor, zone), endOfDayExclusiveMillis(anchor, zone), anchor)
    CalendarView.AGENDA -> {
        val last = LocalDate.fromEpochDays(anchor.toEpochDays() + AGENDA_DAYS - 1)
        VisibleRange(startOfDayMillis(anchor, zone), endOfDayExclusiveMillis(last, zone), anchor)
    }
}
