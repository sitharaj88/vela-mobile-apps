/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation.month

import androidx.lifecycle.viewModelScope
import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.model.EventOccurrence
import com.vela.apps.calendar.domain.usecase.ObserveAllEventsUseCase
import com.vela.apps.calendar.domain.usecase.ObserveOccurrencesUseCase
import com.vela.core.common.MviStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

/**
 * Drives every calendar view. The (view, anchor) pair is the source of truth: changing either
 * recomputes the visible range, re-observes occurrences for it (via [flatMapLatest]), and — for Month
 * — rebuilds the 6x7 grid. A separate all-events stream powers search.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarStore(
    observeOccurrences: ObserveOccurrencesUseCase,
    observeAllEvents: ObserveAllEventsUseCase,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) : MviStore<CalendarState, CalendarIntent, CalendarEffect>(initialState()) {

    private data class Period(val view: CalendarView, val anchor: LocalDate)

    private val period = MutableStateFlow(Period(currentState.view, currentState.anchor))

    /** Latest full event list (for search); filtered on demand without re-querying. */
    private var allEvents: List<Event> = emptyList()

    init {
        period
            .flatMapLatest { (view, anchor) ->
                setState { withPeriod(view, anchor) }
                val range = visibleRange(view, anchor, zone)
                observeOccurrences(range.startMillis, range.endMillis, zone)
            }
            .onEach { occurrences -> setState { withOccurrences(occurrences) } }
            .launchIn(viewModelScope)

        observeAllEvents()
            .onEach { events ->
                allEvents = events
                if (currentState.isSearching) setState { withSearchResults(filterEvents(currentState.searchQuery)) }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: CalendarIntent) {
        when (intent) {
            CalendarIntent.Prev -> step(-1)
            CalendarIntent.Next -> step(1)
            CalendarIntent.GoToToday -> {
                val now = today()
                setState { copy(selectedDate = now) }
                period.value = Period(period.value.view, now)
            }
            is CalendarIntent.SelectView -> period.value = Period(intent.view, period.value.anchor)
            is CalendarIntent.SelectDate -> selectDate(intent.date)
            is CalendarIntent.Search -> search(intent.query)
            CalendarIntent.ClearSearch ->
                setState { copy(searchQuery = "", searchResults = emptyList()) }
            is CalendarIntent.AddEvent ->
                emitEffect(CalendarEffect.OpenEditor(NEW_EVENT_ID, intent.date.toEpochDays().toLong()))
            is CalendarIntent.OpenEvent ->
                emitEffect(CalendarEffect.OpenEditor(intent.id, currentState.selectedDate.toEpochDays().toLong()))
        }
    }

    private fun selectDate(date: LocalDate) {
        setState { copy(selectedDate = date) }
        // Read the live (view, anchor) from `period` — currentState lags because it is only updated
        // asynchronously inside the flatMapLatest collector, so back-to-back intents would see stale
        // values. In Week/Day views, selecting a date re-anchors the visible period to it.
        val current = period.value
        if (current.view == CalendarView.DAY || current.view == CalendarView.WEEK) {
            period.value = Period(current.view, date)
        } else if (date.year != current.anchor.year || date.monthNumber != current.anchor.monthNumber) {
            period.value = Period(current.view, date)
        }
    }

    private fun search(query: String) {
        setState { copy(searchQuery = query, searchResults = filterEvents(query)) }
    }

    private fun filterEvents(query: String): List<Event> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        return allEvents.filter { event ->
            event.title.contains(q, ignoreCase = true) ||
                event.description.contains(q, ignoreCase = true) ||
                event.location.contains(q, ignoreCase = true)
        }
    }

    private fun step(delta: Int) {
        val anchor = period.value.anchor
        val next = when (period.value.view) {
            CalendarView.MONTH -> anchor.plus(delta, DateTimeUnit.MONTH)
            CalendarView.WEEK -> anchor.plus(delta, DateTimeUnit.WEEK)
            CalendarView.DAY -> anchor.plus(delta, DateTimeUnit.DAY)
            CalendarView.AGENDA -> anchor.plus(delta * AGENDA_PAGE_DAYS, DateTimeUnit.DAY)
        }
        period.value = Period(currentState.view, next)
    }

    private fun CalendarState.withPeriod(view: CalendarView, anchor: LocalDate): CalendarState {
        val weeks = if (view == CalendarView.MONTH) monthGrid(anchor.year, anchor.monthNumber) else weeks
        // Keep the selection inside Day/Week so the highlighted day matches the shown period.
        val selected = if (view == CalendarView.DAY) anchor else selectedDate
        return copy(view = view, anchor = anchor, weeks = weeks, selectedDate = selected)
    }

    private fun CalendarState.withOccurrences(occurrences: List<EventOccurrence>): CalendarState =
        copy(
            occurrences = occurrences,
            datesWithEvents = occurrences
                .map { localDateOf(it.startMillis, zone).toEpochDays().toLong() }
                .toSet(),
        )

    private fun CalendarState.withSearchResults(results: List<Event>): CalendarState =
        copy(searchResults = results)

    private companion object {
        const val NEW_EVENT_ID = 0L
        const val AGENDA_PAGE_DAYS = 30

        fun initialState(): CalendarState {
            val now = today()
            return CalendarState(anchor = now, selectedDate = now)
        }
    }
}
