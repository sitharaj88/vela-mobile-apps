/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.platform

import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.reminder.EventReminderScheduler
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual fun calendarPlatformModule(): Module = module {
    singleOf(::IosReminderScheduler) bind EventReminderScheduler::class
}

/**
 * Best-effort iOS scheduler backed by [UNUserNotificationCenter]. iOS only compiles on macOS, so the
 * cinterop calls here are unverified; uncertain APIs are marked `// TODO(ios)`.
 */
class IosReminderScheduler : EventReminderScheduler {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override fun schedule(event: Event) {
        val minutes = event.reminderMinutes
        if (minutes == null) {
            cancel(event.id)
            return
        }
        val triggerAtSeconds = event.startMillis / MILLIS_PER_SECOND - minutes * SECONDS_PER_MINUTE
        // TODO(ios): verify NSDate.timeIntervalSince1970 accessor shape on a macOS toolchain.
        val nowSeconds = NSDate().timeIntervalSince1970()
        val intervalSeconds = triggerAtSeconds - nowSeconds.toLong()
        if (intervalSeconds <= 0) {
            cancel(event.id)
            return
        }

        val content = UNMutableNotificationContent().apply {
            setTitle(event.title.ifBlank { "Event" })
            setBody("Starts soon")
        }
        // TODO(ios): verify trigger factory + completion-handler signature / authorization on macOS.
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = intervalSeconds.toDouble(),
            repeats = false,
        )
        val request = UNNotificationRequest.requestWithIdentifier(event.id.toString(), content, trigger)
        center.addNotificationRequest(request, null)
    }

    override fun cancel(eventId: Long) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(eventId.toString()))
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000L
        const val SECONDS_PER_MINUTE = 60L
    }
}
