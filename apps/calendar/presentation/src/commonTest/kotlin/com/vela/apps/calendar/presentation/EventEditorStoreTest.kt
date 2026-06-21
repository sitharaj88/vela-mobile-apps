/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation

import app.cash.turbine.test
import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.model.Recurrence
import com.vela.apps.calendar.domain.usecase.DeleteEventUseCase
import com.vela.apps.calendar.domain.usecase.ObserveEventUseCase
import com.vela.apps.calendar.domain.usecase.SaveEventUseCase
import com.vela.apps.calendar.presentation.editor.EventEditorEffect
import com.vela.apps.calendar.presentation.editor.EventEditorIntent
import com.vela.apps.calendar.presentation.editor.EventEditorStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EventEditorStoreTest {

    private val dispatcher = StandardTestDispatcher()
    private val utc = TimeZone.UTC
    private lateinit var repository: FakeEventRepository
    private lateinit var scheduler: FakeReminderScheduler

    private val seedEpochDay = LocalDate(2026, 6, 15).toEpochDays().toLong()

    private fun store(eventId: Long): EventEditorStore = EventEditorStore(
        eventId = eventId,
        seedEpochDay = seedEpochDay,
        observeEvent = ObserveEventUseCase(repository),
        saveEvent = SaveEventUseCase(repository, scheduler),
        deleteEvent = DeleteEventUseCase(repository, scheduler),
        zone = utc,
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeEventRepository()
        scheduler = FakeReminderScheduler()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun new_event_seeds_start_and_end_on_the_target_day() = runTest(dispatcher) {
        val store = store(0L)
        testScheduler.advanceUntilIdle()
        assertEquals(LocalDate(2026, 6, 15), store.state.value.startDate)
        assertEquals(LocalDate(2026, 6, 15), store.state.value.endDate)
        assertTrue(store.state.value.isNew)
    }

    @Test
    fun close_saves_event_and_navigates_back() = runTest(dispatcher) {
        val store = store(0L)
        store.effects.test {
            store.onIntent(EventEditorIntent.TitleChanged("Lunch"))
            store.onIntent(EventEditorIntent.Close)
            testScheduler.advanceUntilIdle()
            assertEquals(EventEditorEffect.NavigateBack, awaitItem())
        }
        assertEquals(1, repository.events.value.size)
        assertEquals("Lunch", repository.events.value.first().title)
    }

    @Test
    fun blank_title_is_not_persisted() = runTest(dispatcher) {
        val store = store(0L)
        store.onIntent(EventEditorIntent.Close)
        testScheduler.advanceUntilIdle()
        assertTrue(repository.events.value.isEmpty())
    }

    @Test
    fun saving_with_reminder_schedules_it() = runTest(dispatcher) {
        val store = store(0L)
        store.onIntent(EventEditorIntent.TitleChanged("Meeting"))
        store.onIntent(EventEditorIntent.ReminderChanged(15))
        store.onIntent(EventEditorIntent.Close)
        testScheduler.advanceUntilIdle()

        assertEquals(1, scheduler.scheduled.size)
        assertEquals(15, scheduler.scheduled.first().reminderMinutes)
    }

    @Test
    fun saving_without_reminder_cancels_any_existing() = runTest(dispatcher) {
        val store = store(0L)
        store.onIntent(EventEditorIntent.TitleChanged("Meeting"))
        store.onIntent(EventEditorIntent.Close)
        testScheduler.advanceUntilIdle()

        assertTrue(scheduler.scheduled.isEmpty())
        assertEquals(1, scheduler.cancelled.size)
    }

    @Test
    fun loading_existing_event_populates_all_fields() = runTest(dispatcher) {
        repository.save(
            Event(
                id = 7,
                title = "Trip",
                location = "Airport",
                startMillis = 0,
                endMillis = 0,
                colorIndex = 3,
                recurrence = Recurrence.WEEKLY,
                reminderMinutes = 30,
            ),
        )
        val store = store(7L)
        testScheduler.advanceUntilIdle()

        val state = store.state.value
        assertEquals("Trip", state.title)
        assertEquals("Airport", state.location)
        assertEquals(3, state.colorIndex)
        assertEquals(Recurrence.WEEKLY, state.recurrence)
        assertEquals(30, state.reminderMinutes)
        assertTrue(state.loaded)
    }

    @Test
    fun delete_removes_event_cancels_reminder_and_navigates() = runTest(dispatcher) {
        repository.save(Event(id = 9, title = "Old", startMillis = 0, endMillis = 0))
        val store = store(9L)
        store.effects.test {
            testScheduler.advanceUntilIdle()
            store.onIntent(EventEditorIntent.Delete)
            testScheduler.advanceUntilIdle()
            assertEquals(EventEditorEffect.NavigateBack, awaitItem())
        }
        assertTrue(repository.events.value.isEmpty())
        assertTrue(scheduler.cancelled.contains(9L))
    }
}
