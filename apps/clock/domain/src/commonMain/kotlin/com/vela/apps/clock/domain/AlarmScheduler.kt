/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.domain

/**
 * Platform boundary for delivering an alarm at its scheduled time. Implemented per-platform
 * (AlarmManager on Android, an in-process coroutine + SystemTray on Desktop, UNUserNotificationCenter
 * on iOS) and provided via `clockPlatformModule()`.
 */
interface AlarmScheduler {
    /** Registers (or re-registers) [alarm] to fire at its next occurrence. No-op if disabled. */
    fun schedule(alarm: Alarm)

    /** Cancels any pending delivery for the alarm with [alarmId]. */
    fun cancel(alarmId: Long)
}
