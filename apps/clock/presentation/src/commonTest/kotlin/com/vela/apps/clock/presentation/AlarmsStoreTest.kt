/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.presentation

import com.vela.apps.clock.domain.Alarm
import com.vela.apps.clock.domain.AlarmRepository
import com.vela.apps.clock.domain.AlarmScheduler
import com.vela.apps.clock.domain.WeekDay
import com.vela.apps.clock.presentation.alarms.AlarmsIntent
import com.vela.apps.clock.presentation.alarms.AlarmsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmsStoreTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeAlarmRepository : AlarmRepository {
        val alarms = MutableStateFlow<List<Alarm>>(emptyList())
        var nextId = 1L
        override fun observeAlarms(): Flow<List<Alarm>> = alarms
        override suspend fun save(alarm: Alarm): Long {
            val id = if (alarm.id == 0L) nextId++ else alarm.id
            val stored = alarm.copy(id = id)
            alarms.value = alarms.value.filterNot { it.id == id } + stored
            return id
        }
        override suspend fun setEnabled(alarmId: Long, enabled: Boolean) {
            alarms.value = alarms.value.map { if (it.id == alarmId) it.copy(enabled = enabled) else it }
        }
        override suspend fun delete(alarmId: Long) {
            alarms.value = alarms.value.filterNot { it.id == alarmId }
        }
    }

    private class FakeScheduler : AlarmScheduler {
        val scheduled = mutableListOf<Alarm>()
        val cancelled = mutableListOf<Long>()
        override fun schedule(alarm: Alarm) { scheduled += alarm }
        override fun cancel(alarmId: Long) { cancelled += alarmId }
    }

    private lateinit var repository: FakeAlarmRepository
    private lateinit var scheduler: FakeScheduler
    private lateinit var store: AlarmsStore

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAlarmRepository()
        scheduler = FakeScheduler()
        store = AlarmsStore(repository, scheduler)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun observes_repository_alarms() = runTest(dispatcher) {
        repository.alarms.value = listOf(Alarm(id = 1, hour = 6, minute = 0))
        testScheduler.advanceUntilIdle()
        assertEquals(1, store.state.value.alarms.size)
    }

    @Test
    fun add_new_opens_editor_with_defaults() = runTest(dispatcher) {
        store.onIntent(AlarmsIntent.AddNew)
        testScheduler.advanceUntilIdle()
        assertEquals(0L, store.state.value.editor?.id)
    }

    @Test
    fun toggling_draft_day_normalizes_order() = runTest(dispatcher) {
        store.onIntent(AlarmsIntent.AddNew)
        store.onIntent(AlarmsIntent.ToggleDraftDay(WeekDay.FRIDAY))
        store.onIntent(AlarmsIntent.ToggleDraftDay(WeekDay.MONDAY))
        testScheduler.advanceUntilIdle()
        assertEquals(
            listOf(WeekDay.MONDAY, WeekDay.FRIDAY),
            store.state.value.editor?.repeatDays?.toList(),
        )
    }

    @Test
    fun save_draft_persists_and_schedules() = runTest(dispatcher) {
        store.onIntent(AlarmsIntent.AddNew)
        store.onIntent(AlarmsIntent.DraftTime(8, 15))
        store.onIntent(AlarmsIntent.DraftLabel("Work"))
        store.onIntent(AlarmsIntent.SaveDraft)
        testScheduler.advanceUntilIdle()

        assertNull(store.state.value.editor)
        assertEquals(1, repository.alarms.value.size)
        val saved = repository.alarms.value.first()
        assertEquals(8, saved.hour)
        assertEquals("Work", saved.label)
        assertEquals(1, scheduler.scheduled.size)
        assertTrue(scheduler.scheduled.first().id > 0)
    }

    @Test
    fun toggle_enabled_off_cancels_schedule() = runTest(dispatcher) {
        repository.alarms.value = listOf(Alarm(id = 5, hour = 6, minute = 0, enabled = true))
        testScheduler.advanceUntilIdle()

        store.onIntent(AlarmsIntent.ToggleEnabled(5, enabled = false))
        testScheduler.advanceUntilIdle()

        assertEquals(false, repository.alarms.value.first().enabled)
        assertTrue(5L in scheduler.cancelled)
    }

    @Test
    fun delete_removes_and_cancels() = runTest(dispatcher) {
        repository.alarms.value = listOf(Alarm(id = 9, hour = 7, minute = 0))
        testScheduler.advanceUntilIdle()

        store.onIntent(AlarmsIntent.Delete(9))
        testScheduler.advanceUntilIdle()

        assertTrue(repository.alarms.value.isEmpty())
        assertTrue(9L in scheduler.cancelled)
    }
}
