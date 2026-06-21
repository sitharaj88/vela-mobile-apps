/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.platform

import org.koin.core.module.Module

/**
 * Provides the platform [com.vela.apps.calendar.domain.reminder.EventReminderScheduler]:
 * - Android: `AlarmManager` schedules an exact alarm; [ReminderReceiver] posts the notification.
 * - Desktop: an in-process coroutine waits until the reminder time and shows a `SystemTray` notification.
 * - iOS: best-effort `UNUserNotificationCenter` (see `// TODO(ios)` markers).
 */
expect fun calendarPlatformModule(): Module
