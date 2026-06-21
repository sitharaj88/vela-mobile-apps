/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation

import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.reminder.EventReminderScheduler
import com.vela.apps.calendar.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [EventRepository] for store tests. Range filtering mirrors the DAO's candidate query. */
class FakeEventRepository(initial: List<Event> = emptyList()) : EventRepository {
    val events = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun observeInRange(startMillis: Long, endMillis: Long): Flow<List<Event>> =
        events.map { list ->
            list.filter { e ->
                if (e.recurrence == com.vela.apps.calendar.domain.model.Recurrence.NONE) {
                    e.endMillis > startMillis && e.startMillis < endMillis
                } else {
                    e.startMillis < endMillis
                }
            }
        }

    override fun observeAll(): Flow<List<Event>> = events.map { it.sortedByDescending(Event::startMillis) }

    override fun observeEvent(id: Long): Flow<Event?> = events.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getEvent(id: Long): Event? = events.value.firstOrNull { it.id == id }

    override suspend fun save(event: Event): Long {
        val id = if (event.id == 0L) nextId++ else event.id
        val saved = event.copy(id = id)
        events.value = events.value.filterNot { it.id == id } + saved
        return id
    }

    override suspend fun delete(id: Long) {
        events.value = events.value.filterNot { it.id == id }
    }
}

/** Records scheduler interactions so tests can assert reminder scheduling/cancellation. */
class FakeReminderScheduler : EventReminderScheduler {
    val scheduled = mutableListOf<Event>()
    val cancelled = mutableListOf<Long>()

    override fun schedule(event: Event) {
        scheduled += event
    }

    override fun cancel(eventId: Long) {
        cancelled += eventId
    }
}
