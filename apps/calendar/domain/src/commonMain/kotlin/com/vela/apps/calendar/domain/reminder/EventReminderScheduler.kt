/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.domain.reminder

import com.vela.apps.calendar.domain.model.Event

/**
 * Platform boundary for delivering a reminder notification ahead of an event. Implemented per-platform
 * and provided via `calendarPlatformModule()`:
 * - Android: `AlarmManager` schedules an exact alarm that posts a notification.
 * - Desktop: an in-process coroutine waits until the reminder time and shows a `SystemTray` notification.
 * - iOS: best-effort `UNUserNotificationCenter` (see `// TODO(ios)` markers).
 *
 * Reminders fire `event.reminderMinutes` before `event.startMillis`. Implementations no-op when the
 * event has no reminder or the reminder time has already passed.
 */
interface EventReminderScheduler {
    /** Registers (or re-registers) a reminder for [event]. Cancels any prior reminder for the same id. */
    fun schedule(event: Event)

    /** Cancels any pending reminder for the event with [eventId]. */
    fun cancel(eventId: Long)
}
