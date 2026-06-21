/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation.month

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/** Calendar grids are Monday-first, six weeks of seven days. */
internal const val DAYS_PER_WEEK = 7
internal const val WEEKS_PER_GRID = 6
private const val CELLS_PER_GRID = DAYS_PER_WEEK * WEEKS_PER_GRID

/** Today's date in the system time zone. */
fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

/** Weekday header labels (Mon..Sun), three letters, title-cased — KMP-safe (no String.format). */
fun weekdayLabels(): List<String> = DayOfWeek.entries.map { it.name.titleCaseShort() }

/** Full month name for [date], title-cased. */
fun monthLabel(date: LocalDate): String = date.month.name.titleCase()

/** Short month name for [date], three letters, title-cased. */
fun monthLabelShort(date: LocalDate): String = date.month.name.titleCaseShort()

/** Short weekday name for [date], three letters, title-cased. */
fun weekdayLabelShort(date: LocalDate): String = date.dayOfWeek.name.titleCaseShort()

/**
 * Build a Monday-first 6x7 grid of dates covering [year]/[month], padding with the trailing days of
 * the previous month and the leading days of the next month so every cell is filled.
 */
fun monthGrid(year: Int, month: Int): List<List<LocalDate>> {
    val first = LocalDate(year, month, 1)
    // DayOfWeek.isoDayNumber: Monday = 1 .. Sunday = 7. Back up to the Monday on/just before the 1st.
    val leading = first.dayOfWeek.isoDayNumber - 1
    val gridStart = first.minus(leading, DateTimeUnit.DAY)
    return (0 until CELLS_PER_GRID)
        .map { offset -> gridStart.plus(offset, DateTimeUnit.DAY) }
        .chunked(DAYS_PER_WEEK)
}

/** The Monday on or before [date] — the start of [date]'s ISO week (Monday-first). */
fun weekStart(date: LocalDate): LocalDate =
    date.minus(date.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)

/** The seven Monday-first dates of the week containing [date]. */
fun weekDays(date: LocalDate): List<LocalDate> {
    val start = weekStart(date)
    return (0 until DAYS_PER_WEEK).map { start.plus(it, DateTimeUnit.DAY) }
}

/**
 * ISO-8601 week number (1..53) of [date]. The week containing the year's first Thursday is week 1.
 * Pure and tested.
 */
fun weekOfYear(date: LocalDate): Int {
    // Thursday of this week determines the owning ISO year, hence the week number.
    val thursday = weekStart(date).plus(THURSDAY_OFFSET, DateTimeUnit.DAY)
    val firstThursday = firstIsoThursday(thursday.year)
    val daysBetween = thursday.toEpochDays() - firstThursday.toEpochDays()
    return (daysBetween / DAYS_PER_WEEK).toInt() + 1
}

private fun firstIsoThursday(year: Int): LocalDate {
    val jan1 = LocalDate(year, 1, 1)
    val offsetToThursday = (DayOfWeek.THURSDAY.isoDayNumber - jan1.dayOfWeek.isoDayNumber + DAYS_PER_WEEK) %
        DAYS_PER_WEEK
    return jan1.plus(offsetToThursday, DateTimeUnit.DAY)
}

private const val THURSDAY_OFFSET = 3

private val DayOfWeek.isoDayNumber: Int get() = ordinal + 1

private fun String.titleCase(): String =
    lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

private fun String.titleCaseShort(): String = titleCase().take(3)
