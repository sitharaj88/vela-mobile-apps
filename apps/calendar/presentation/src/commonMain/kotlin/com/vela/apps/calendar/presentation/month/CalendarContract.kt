/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation.month

import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.model.EventOccurrence
import kotlinx.datetime.LocalDate

/**
 * State for the calendar screen across all views (Month / Week / Day / Agenda).
 *
 * @param view the active view mode.
 * @param anchor the date the active view is centered on (drives both the rendered period and range).
 * @param selectedDate the focused day (its events appear in the Month view's day list).
 * @param weeks the 6x7 Month grid (Monday-first); recomputed when the anchor month changes.
 * @param occurrences expanded occurrences for the whole visible range (recurring events included).
 * @param datesWithEvents epoch-days (within the visible range) that have at least one occurrence.
 * @param searchQuery active search text; when non-blank, [searchResults] is shown instead of a view.
 * @param searchResults events whose title/description/location match [searchQuery].
 */
data class CalendarState(
    val view: CalendarView = CalendarView.MONTH,
    val anchor: LocalDate,
    val selectedDate: LocalDate,
    val weeks: List<List<LocalDate>> = emptyList(),
    val occurrences: List<EventOccurrence> = emptyList(),
    val datesWithEvents: Set<Long> = emptySet(),
    val searchQuery: String = "",
    val searchResults: List<Event> = emptyList(),
) {
    val isSearching: Boolean get() = searchQuery.isNotBlank()

    /** Title for the header, depending on the active view. */
    val periodTitle: String
        get() = when (view) {
            CalendarView.MONTH -> "${monthLabel(monthReference)} ${anchor.year}"
            CalendarView.WEEK -> "Week ${weekOfYear(anchor)} · ${anchor.year}"
            CalendarView.DAY ->
                "${weekdayLabelShort(anchor)}, ${monthLabelShort(anchor)} ${anchor.dayOfMonth} ${anchor.year}"
            CalendarView.AGENDA -> "Agenda"
        }

    /** Occurrences whose start day equals [selectedDate] (Month view day list). */
    val selectedDayOccurrences: List<EventOccurrence>
        get() {
            val epoch = selectedDate.toEpochDays().toLong()
            return occurrences.filter { localDateOf(it.startMillis).toEpochDays().toLong() == epoch }
        }

    private val monthReference: LocalDate get() = LocalDate(anchor.year, anchor.monthNumber, 1)
}

sealed interface CalendarIntent {
    data object Prev : CalendarIntent
    data object Next : CalendarIntent
    data object GoToToday : CalendarIntent
    data class SelectView(val view: CalendarView) : CalendarIntent
    data class SelectDate(val date: LocalDate) : CalendarIntent
    data class Search(val query: String) : CalendarIntent
    data object ClearSearch : CalendarIntent
    data class AddEvent(val date: LocalDate) : CalendarIntent
    data class OpenEvent(val id: Long) : CalendarIntent
}

sealed interface CalendarEffect {
    /** Open the editor for [eventId] (0 = new); [epochDay] seeds a new event's date. */
    data class OpenEditor(val eventId: Long, val epochDay: Long) : CalendarEffect
}
