/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.reminder.EventReminderScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun calendarPlatformModule(): Module = module {
    single { AndroidReminderScheduler(androidContext()) } bind EventReminderScheduler::class
}

/**
 * Schedules an exact alarm with [AlarmManager] for `reminderMinutes` before the event start. On fire,
 * [ReminderReceiver] posts the notification. No-op if the reminder time has already passed.
 */
class AndroidReminderScheduler(
    private val context: Context,
) : EventReminderScheduler {

    private val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(event: Event) {
        val minutes = event.reminderMinutes
        if (minutes == null) {
            cancel(event.id)
            return
        }
        val triggerAtMs = event.startMillis - minutes * MILLIS_PER_MINUTE
        if (triggerAtMs <= System.currentTimeMillis()) {
            cancel(event.id)
            return
        }
        val pending = pendingIntent(event.id, event.title, event.startMillis)
        manager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMs, pending), pending)
    }

    override fun cancel(eventId: Long) {
        manager.cancel(pendingIntent(eventId, title = "", startMillis = 0))
    }

    private fun pendingIntent(eventId: Long, title: String, startMillis: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_FIRE
            putExtra(ReminderReceiver.EXTRA_ID, eventId)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_START, startMillis)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, eventId.toInt(), intent, flags)
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
