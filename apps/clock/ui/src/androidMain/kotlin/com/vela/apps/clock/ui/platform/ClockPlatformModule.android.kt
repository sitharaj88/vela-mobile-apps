/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.vela.apps.clock.domain.Alarm
import com.vela.apps.clock.domain.AlarmScheduler
import com.vela.apps.clock.domain.nextAlarmTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun clockPlatformModule(): Module = module {
    single { AndroidAlarmScheduler(androidContext()) } bind AlarmScheduler::class
}

/**
 * Schedules a one-off exact alarm with [AlarmManager]; on fire, [AlarmReceiver] posts a notification
 * and (for repeating alarms) re-schedules the next occurrence.
 */
class AndroidAlarmScheduler(
    private val context: Context,
) : AlarmScheduler {

    private val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        if (!alarm.enabled) {
            cancel(alarm.id)
            return
        }
        val next = nextAlarmTime(alarm, Clock.System.now(), TimeZone.currentSystemDefault()) ?: return
        val pending = pendingIntent(alarm)
        val triggerAtMs = next.toEpochMilliseconds()
        // setAlarmClock surfaces in the system UI and is exact even in Doze.
        manager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMs, pending), pending)
    }

    override fun cancel(alarmId: Long) {
        manager.cancel(pendingIntent(Alarm(id = alarmId, hour = 0, minute = 0)))
    }

    private fun pendingIntent(alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(AlarmReceiver.EXTRA_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_HOUR, alarm.hour)
            putExtra(AlarmReceiver.EXTRA_MINUTE, alarm.minute)
            putExtra(AlarmReceiver.EXTRA_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_REPEAT, alarm.repeatDays.map { it.ordinal }.toIntArray())
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, alarm.id.toInt(), intent, flags)
    }
}
