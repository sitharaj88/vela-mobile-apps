/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.domain.usecase

import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.model.EventOccurrence
import com.vela.apps.calendar.domain.recurrence.expandOccurrences
import com.vela.apps.calendar.domain.reminder.EventReminderScheduler
import com.vela.apps.calendar.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone

/**
 * Observe the concrete event occurrences that intersect `[startMillis, endMillis)` in [timeZone],
 * expanding recurring masters via the pure [expandOccurrences] logic.
 */
class ObserveOccurrencesUseCase(private val repository: EventRepository) {
    operator fun invoke(
        startMillis: Long,
        endMillis: Long,
        timeZone: TimeZone,
    ): Flow<List<EventOccurrence>> =
        repository.observeInRange(startMillis, endMillis).map { events ->
            expandOccurrences(events, startMillis, endMillis, timeZone)
        }
}

/** Observe every event, used by search; filtering is performed in the store. */
class ObserveAllEventsUseCase(private val repository: EventRepository) {
    operator fun invoke(): Flow<List<Event>> = repository.observeAll()
}

/** Observe a single event by id (null while it doesn't exist yet). */
class ObserveEventUseCase(private val repository: EventRepository) {
    operator fun invoke(id: Long): Flow<Event?> = repository.observeEvent(id)
}

/**
 * Persist an event and (re)schedule its reminder. Blank events (empty title) are not saved, and an
 * existing event emptied to blank is deleted — mirroring Notes' "don't keep empty entries" behavior.
 * Returns the surviving id, or null if the event was discarded/deleted.
 */
class SaveEventUseCase(
    private val repository: EventRepository,
    private val reminderScheduler: EventReminderScheduler,
) {
    suspend operator fun invoke(event: Event): Long? {
        val trimmed = event.copy(
            title = event.title.trim(),
            description = event.description.trim(),
            location = event.location.trim(),
        )
        if (trimmed.isBlank) {
            if (trimmed.id != 0L) {
                reminderScheduler.cancel(trimmed.id)
                repository.delete(trimmed.id)
            }
            return null
        }
        val id = repository.save(trimmed)
        val saved = trimmed.copy(id = id)
        if (saved.hasReminder) reminderScheduler.schedule(saved) else reminderScheduler.cancel(id)
        return id
    }
}

/** Delete an event and cancel any pending reminder. */
class DeleteEventUseCase(
    private val repository: EventRepository,
    private val reminderScheduler: EventReminderScheduler,
) {
    suspend operator fun invoke(id: Long) {
        reminderScheduler.cancel(id)
        repository.delete(id)
    }
}
