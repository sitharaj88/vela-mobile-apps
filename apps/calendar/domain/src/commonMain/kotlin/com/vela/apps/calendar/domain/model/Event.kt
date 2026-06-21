/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.domain.model

/**
 * A calendar event. [id] == 0 denotes a not-yet-persisted event.
 *
 * Times are stored as epoch milliseconds (UTC instant) for [startMillis]/[endMillis]; the UI converts
 * to the system time zone for display. All-day events still carry a start/end instant (midnight..next
 * midnight) so range queries stay uniform, but render without a time component.
 *
 * Recurrence is described by [recurrence]; recurring events are expanded into [EventOccurrence]s for a
 * visible range by [com.vela.apps.calendar.domain.recurrence.expandOccurrences] (pure + testable).
 *
 * [reminderMinutes] (when non-null) requests a reminder that many minutes before [startMillis].
 */
data class Event(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startMillis: Long = 0,
    val endMillis: Long = 0,
    val allDay: Boolean = false,
    val colorIndex: Int = 0,
    val recurrence: Recurrence = Recurrence.NONE,
    val reminderMinutes: Int? = null,
) {
    val isBlank: Boolean get() = title.isBlank()

    /** Whether a reminder should be scheduled for this event. */
    val hasReminder: Boolean get() = reminderMinutes != null

    companion object {
        /** Number of selectable color labels; [colorIndex] is taken modulo this. */
        const val COLOR_COUNT = 6
    }
}
