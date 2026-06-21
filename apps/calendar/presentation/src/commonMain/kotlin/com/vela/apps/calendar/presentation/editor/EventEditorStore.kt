/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation.editor

import androidx.lifecycle.viewModelScope
import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.usecase.DeleteEventUseCase
import com.vela.apps.calendar.domain.usecase.ObserveEventUseCase
import com.vela.apps.calendar.domain.usecase.SaveEventUseCase
import com.vela.apps.calendar.presentation.month.endOfDayExclusiveMillis
import com.vela.apps.calendar.presentation.month.localDateTimeOf
import com.vela.apps.calendar.presentation.month.millisAt
import com.vela.apps.calendar.presentation.month.startOfDayMillis
import com.vela.core.common.MviStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * Edits a single event. An [eventId] of 0 starts a new event seeded on [seedEpochDay]. The event is
 * auto-saved on [EventEditorIntent.Close] (and discarded if its title is left blank), mirroring Notes.
 * Saving (re)schedules the reminder via [SaveEventUseCase].
 */
class EventEditorStore(
    private val eventId: Long,
    seedEpochDay: Long,
    private val observeEvent: ObserveEventUseCase,
    private val saveEvent: SaveEventUseCase,
    private val deleteEvent: DeleteEventUseCase,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) : MviStore<EventEditorState, EventEditorIntent, EventEditorEffect>(
    initialState(eventId, seedEpochDay),
) {

    init {
        if (eventId != 0L) {
            viewModelScope.launch {
                observeEvent(eventId).first()?.let { event -> setState { fromEvent(event) } }
            }
        }
    }

    override fun onIntent(intent: EventEditorIntent) {
        when (intent) {
            is EventEditorIntent.TitleChanged -> setState { copy(title = intent.title) }
            is EventEditorIntent.DescriptionChanged -> setState { copy(description = intent.description) }
            is EventEditorIntent.LocationChanged -> setState { copy(location = intent.location) }
            is EventEditorIntent.StartDateChanged -> setState { withStartDate(intent.date) }
            is EventEditorIntent.StartTimeChanged -> setState { copy(startTime = intent.time) }
            is EventEditorIntent.EndDateChanged -> setState { copy(endDate = maxDate(startDate, intent.date)) }
            is EventEditorIntent.EndTimeChanged -> setState { copy(endTime = intent.time) }
            is EventEditorIntent.AllDayChanged -> setState { copy(allDay = intent.allDay) }
            is EventEditorIntent.ColorChanged -> setState { copy(colorIndex = intent.colorIndex) }
            is EventEditorIntent.RecurrenceChanged -> setState { copy(recurrence = intent.recurrence) }
            is EventEditorIntent.ReminderChanged -> setState { copy(reminderMinutes = intent.minutes) }
            EventEditorIntent.Delete -> viewModelScope.launch {
                if (!currentState.isNew) deleteEvent(currentState.id)
                emitEffect(EventEditorEffect.NavigateBack)
            }
            EventEditorIntent.Close -> viewModelScope.launch {
                saveEvent(currentState.toEvent(zone))
                emitEffect(EventEditorEffect.NavigateBack)
            }
        }
    }

    /** Keep end on/after start when the start date moves earlier than the end. */
    private fun EventEditorState.withStartDate(date: LocalDate): EventEditorState =
        copy(startDate = date, endDate = maxDate(date, endDate))

    private fun EventEditorState.fromEvent(event: Event): EventEditorState {
        val start = localDateTimeOf(event.startMillis, zone)
        val end = localDateTimeOf(event.endMillis, zone)
        return copy(
            title = event.title,
            description = event.description,
            location = event.location,
            startDate = start.date,
            startTime = start.time,
            endDate = end.date,
            endTime = end.time,
            allDay = event.allDay,
            colorIndex = event.colorIndex,
            recurrence = event.recurrence,
            reminderMinutes = event.reminderMinutes,
            loaded = true,
        )
    }

    private companion object {
        fun initialState(eventId: Long, seedEpochDay: Long): EventEditorState {
            val date = LocalDate.fromEpochDays(seedEpochDay.toInt())
            return EventEditorState(id = eventId, startDate = date, endDate = date, loaded = eventId == 0L)
        }

        fun maxDate(a: LocalDate, b: LocalDate): LocalDate = if (b < a) a else b
    }
}

/** Convert editor state to a domain [Event], snapping all-day events to whole-day instants. */
fun EventEditorState.toEvent(zone: TimeZone): Event {
    val startMillis: Long
    val endMillis: Long
    if (allDay) {
        startMillis = startOfDayMillis(startDate, zone)
        endMillis = endOfDayExclusiveMillis(endDate, zone)
    } else {
        startMillis = millisAt(startDate, startTime, zone)
        val rawEnd = millisAt(endDate, endTime, zone)
        // Guard against an end-before-start time by clamping to start.
        endMillis = if (rawEnd < startMillis) startMillis else rawEnd
    }
    return Event(
        id = id,
        title = title,
        description = description,
        location = location,
        startMillis = startMillis,
        endMillis = endMillis,
        allDay = allDay,
        colorIndex = colorIndex,
        recurrence = recurrence,
        reminderMinutes = reminderMinutes,
    )
}
