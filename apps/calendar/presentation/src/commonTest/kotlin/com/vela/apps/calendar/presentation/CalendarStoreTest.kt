/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation

import app.cash.turbine.test
import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.model.Recurrence
import com.vela.apps.calendar.domain.usecase.ObserveAllEventsUseCase
import com.vela.apps.calendar.domain.usecase.ObserveOccurrencesUseCase
import com.vela.apps.calendar.presentation.month.CalendarEffect
import com.vela.apps.calendar.presentation.month.CalendarIntent
import com.vela.apps.calendar.presentation.month.CalendarStore
import com.vela.apps.calendar.presentation.month.CalendarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarStoreTest {

    private val dispatcher = StandardTestDispatcher()
    private val utc = TimeZone.UTC
    private lateinit var repository: FakeEventRepository

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 9): Long =
        LocalDateTime(LocalDate(year, month, day), LocalTime(hour, 0)).toInstant(utc).toEpochMilliseconds()

    private fun store(): CalendarStore {
        val occurrences = ObserveOccurrencesUseCase(repository)
        val all = ObserveAllEventsUseCase(repository)
        return CalendarStore(occurrences, all, zone = utc)
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeEventRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun day_view_shows_only_occurrences_for_the_anchored_day() = runTest(dispatcher) {
        repository.events.value = listOf(
            Event(id = 1, title = "On day", startMillis = millis(2026, 6, 15), endMillis = millis(2026, 6, 15) + 1),
            Event(id = 2, title = "Other day", startMillis = millis(2026, 6, 16), endMillis = millis(2026, 6, 16) + 1),
        )
        val store = store()
        store.onIntent(CalendarIntent.SelectView(CalendarView.DAY))
        store.onIntent(CalendarIntent.SelectDate(LocalDate(2026, 6, 15)))
        testScheduler.advanceUntilIdle()

        val occ = store.state.value.occurrences
        assertEquals(1, occ.size)
        assertEquals(1L, occ.first().event.id)
    }

    @Test
    fun daily_recurrence_expands_in_week_view() = runTest(dispatcher) {
        repository.events.value = listOf(
            Event(
                id = 1,
                title = "Standup",
                startMillis = millis(2026, 6, 15),
                endMillis = millis(2026, 6, 15) + 1,
                recurrence = Recurrence.DAILY,
            ),
        )
        val store = store()
        store.onIntent(CalendarIntent.SelectView(CalendarView.WEEK))
        store.onIntent(CalendarIntent.SelectDate(LocalDate(2026, 6, 15))) // week Mon 15 .. Sun 21
        testScheduler.advanceUntilIdle()

        assertEquals(7, store.state.value.occurrences.size)
    }

    @Test
    fun search_filters_events_by_title_case_insensitively() = runTest(dispatcher) {
        repository.events.value = listOf(
            Event(id = 1, title = "Dentist", startMillis = millis(2026, 6, 15), endMillis = millis(2026, 6, 15) + 1),
            Event(id = 2, title = "Gym", startMillis = millis(2026, 6, 16), endMillis = millis(2026, 6, 16) + 1),
        )
        val store = store()
        testScheduler.advanceUntilIdle()
        store.onIntent(CalendarIntent.Search("dent"))
        testScheduler.advanceUntilIdle()

        assertTrue(store.state.value.isSearching)
        assertEquals(listOf(1L), store.state.value.searchResults.map { it.id })

        store.onIntent(CalendarIntent.ClearSearch)
        assertTrue(!store.state.value.isSearching)
        assertTrue(store.state.value.searchResults.isEmpty())
    }

    @Test
    fun addEvent_emits_open_editor_for_new_event() = runTest(dispatcher) {
        val store = store()
        store.effects.test {
            store.onIntent(CalendarIntent.AddEvent(LocalDate(2026, 6, 15)))
            val effect = awaitItem()
            assertTrue(effect is CalendarEffect.OpenEditor)
            assertEquals(0L, (effect as CalendarEffect.OpenEditor).eventId)
        }
    }

    @Test
    fun selecting_view_recomputes_period_title() = runTest(dispatcher) {
        val store = store()
        store.onIntent(CalendarIntent.SelectView(CalendarView.DAY))
        store.onIntent(CalendarIntent.SelectDate(LocalDate(2026, 6, 15)))
        testScheduler.advanceUntilIdle()
        assertEquals(CalendarView.DAY, store.state.value.view)
        assertEquals(LocalDate(2026, 6, 15), store.state.value.anchor)
    }
}
