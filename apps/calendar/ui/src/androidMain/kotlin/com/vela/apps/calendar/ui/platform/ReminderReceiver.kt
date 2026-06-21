/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/** Receives the [android.app.AlarmManager] broadcast and posts the event reminder notification. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val id = intent.getLongExtra(EXTRA_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Event" }
        val startMillis = intent.getLongExtra(EXTRA_START, 0L)

        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(startText(startMillis))
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager(context).notify(id.toInt(), notification)
    }

    private fun startText(startMillis: Long): String {
        if (startMillis <= 0L) return "Upcoming event"
        // Avoid String.format / locale APIs; a simple "starts soon" label is enough for the reminder.
        return "Starts soon"
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Event reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager(context).createNotificationChannel(channel)
        }
    }

    private fun notificationManager(context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val ACTION_FIRE = "com.vela.apps.calendar.REMINDER_FIRE"
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_START = "start"
        private const val CHANNEL_ID = "vela_calendar_reminders"
    }
}
