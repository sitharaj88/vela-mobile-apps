/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.vela.apps.clock.domain.Alarm
import com.vela.apps.clock.domain.WeekDay
import kotlinx.collections.immutable.toPersistentList

/**
 * Receives the [AlarmManager] broadcast, posts an alarm notification, and re-arms repeating alarms
 * for their following occurrence.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val id = intent.getLongExtra(EXTRA_ID, 0L)
        val hour = intent.getIntExtra(EXTRA_HOUR, 0)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
        val label = intent.getStringExtra(EXTRA_LABEL).orEmpty()
        val repeatDays = (intent.getIntArrayExtra(EXTRA_REPEAT) ?: IntArray(0))
            .filter { it in WeekDay.entries.indices }
            .map { WeekDay.entries[it] }
            .toPersistentList()

        ensureChannel(context)
        val time = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(label.ifBlank { "Alarm" })
            .setContentText(time)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager(context).notify(id.toInt(), notification)

        if (repeatDays.isNotEmpty()) {
            // Re-arm precisely for the next matching day using the original repeat set.
            AndroidAlarmScheduler(context.applicationContext).schedule(
                Alarm(id = id, hour = hour, minute = minute, label = label, repeatDays = repeatDays),
            )
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH,
            )
            notificationManager(context).createNotificationChannel(channel)
        }
    }

    private fun notificationManager(context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val ACTION_FIRE = "com.vela.apps.clock.ALARM_FIRE"
        const val EXTRA_ID = "id"
        const val EXTRA_HOUR = "hour"
        const val EXTRA_MINUTE = "minute"
        const val EXTRA_LABEL = "label"
        const val EXTRA_REPEAT = "repeat"
        private const val CHANNEL_ID = "vela_clock_alarms"
    }
}
