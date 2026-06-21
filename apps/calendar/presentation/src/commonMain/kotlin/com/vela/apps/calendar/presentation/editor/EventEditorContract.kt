/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation.editor

import com.vela.apps.calendar.domain.model.Recurrence
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Editor state. Times are held as separate [LocalDate]/[LocalTime] so the UI can edit each piece; the
 * store converts to/from epoch millis. [allDay] hides the time pickers and snaps the span to whole days.
 */
data class EventEditorState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startDate: LocalDate,
    val startTime: LocalTime = LocalTime(START_HOUR, 0),
    val endDate: LocalDate,
    val endTime: LocalTime = LocalTime(END_HOUR, 0),
    val allDay: Boolean = false,
    val colorIndex: Int = 0,
    val recurrence: Recurrence = Recurrence.NONE,
    val reminderMinutes: Int? = null,
    val loaded: Boolean = false,
) {
    val isNew: Boolean get() = id == 0L

    companion object {
        const val START_HOUR = 9
        const val END_HOUR = 10
    }
}

/** Reminder presets offered in the editor (minutes before start); null = no reminder. */
val reminderPresets: List<Int?> = listOf(null, 0, 5, 10, 15, 30, 60, 120, 1440)

sealed interface EventEditorIntent {
    data class TitleChanged(val title: String) : EventEditorIntent
    data class DescriptionChanged(val description: String) : EventEditorIntent
    data class LocationChanged(val location: String) : EventEditorIntent
    data class StartDateChanged(val date: LocalDate) : EventEditorIntent
    data class StartTimeChanged(val time: LocalTime) : EventEditorIntent
    data class EndDateChanged(val date: LocalDate) : EventEditorIntent
    data class EndTimeChanged(val time: LocalTime) : EventEditorIntent
    data class AllDayChanged(val allDay: Boolean) : EventEditorIntent
    data class ColorChanged(val colorIndex: Int) : EventEditorIntent
    data class RecurrenceChanged(val recurrence: Recurrence) : EventEditorIntent
    data class ReminderChanged(val minutes: Int?) : EventEditorIntent
    data object Delete : EventEditorIntent

    /** User is leaving the editor; the event is auto-saved (or discarded if its title is blank). */
    data object Close : EventEditorIntent
}

sealed interface EventEditorEffect {
    data object NavigateBack : EventEditorEffect
}
