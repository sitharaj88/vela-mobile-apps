/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.platform

import com.vela.apps.clock.domain.Alarm
import com.vela.apps.clock.domain.AlarmScheduler
import com.vela.apps.clock.domain.WeekDay
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

actual fun clockPlatformModule(): Module = module {
    singleOf(::IosAlarmScheduler) bind AlarmScheduler::class
}

/**
 * Best-effort iOS scheduler backed by [UNUserNotificationCenter]. iOS only compiles on macOS, so the
 * cinterop calls here are unverified; uncertain APIs are marked `// TODO(ios)`.
 */
class IosAlarmScheduler : AlarmScheduler {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override fun schedule(alarm: Alarm) {
        if (!alarm.enabled) {
            cancel(alarm.id)
            return
        }
        val content = UNMutableNotificationContent().apply {
            setTitle(alarm.label.ifBlank { "Alarm" })
            setBody(timeText(alarm))
        }

        if (alarm.repeatDays.isEmpty()) {
            scheduleRequest(alarm.id, content, dateComponents(alarm.hour, alarm.minute), repeats = false)
        } else {
            // One repeating request per weekday (iOS triggers can repeat on a single weekday only).
            alarm.repeatDays.forEach { day ->
                scheduleRequest(
                    requestId = "${alarm.id}_${day.ordinal}",
                    content = content,
                    components = dateComponents(alarm.hour, alarm.minute, day),
                    repeats = true,
                )
            }
        }
    }

    override fun cancel(alarmId: Long) {
        val ids = listOf(alarmId.toString()) + WeekDay.entries.map { "${alarmId}_${it.ordinal}" }
        center.removePendingNotificationRequestsWithIdentifiers(ids)
    }

    private fun scheduleRequest(
        requestId: Long,
        content: UNMutableNotificationContent,
        components: NSDateComponents,
        repeats: Boolean,
    ) = scheduleRequest(requestId.toString(), content, components, repeats)

    private fun scheduleRequest(
        requestId: String,
        content: UNMutableNotificationContent,
        components: NSDateComponents,
        repeats: Boolean,
    ) {
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats)
        val request = UNNotificationRequest.requestWithIdentifier(requestId, content, trigger)
        // TODO(ios): verify completion-handler signature / authorization flow on a macOS toolchain.
        center.addNotificationRequest(request, null)
    }

    private fun dateComponents(hour: Int, minute: Int, day: WeekDay? = null): NSDateComponents {
        val components = NSDateComponents()
        components.setHour(hour.toLong())
        components.setMinute(minute.toLong())
        if (day != null) {
            // iOS weekday: 1 = Sunday .. 7 = Saturday; WeekDay is Mon(0)..Sun(6).
            val iosWeekday = if (day == WeekDay.SUNDAY) 1L else (day.ordinal + 2).toLong()
            components.setWeekday(iosWeekday)
        }
        return components
    }

    private fun timeText(alarm: Alarm): String =
        "${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')}"
}
