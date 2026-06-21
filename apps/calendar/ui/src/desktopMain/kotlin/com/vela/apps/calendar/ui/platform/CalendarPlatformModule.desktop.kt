/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.platform

import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.reminder.EventReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap

actual fun calendarPlatformModule(): Module = module {
    single { DesktopReminderScheduler() } bind EventReminderScheduler::class
}

/**
 * Real in-process scheduler. For each event with a reminder it launches a coroutine that sleeps until
 * `reminderMinutes` before the start and then shows a desktop notification (SystemTray when supported,
 * else a console log). Fires once per scheduled reminder while the app is running.
 */
class DesktopReminderScheduler(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) : EventReminderScheduler {

    private val jobs = ConcurrentHashMap<Long, Job>()
    private val trayIcon: TrayIcon? by lazy(::createTrayIcon)

    override fun schedule(event: Event) {
        cancel(event.id)
        val minutes = event.reminderMinutes ?: return
        val triggerAtMs = event.startMillis - minutes * MILLIS_PER_MINUTE
        val waitMs = triggerAtMs - System.currentTimeMillis()
        if (waitMs <= 0) return
        jobs[event.id] = scope.launch {
            delay(waitMs)
            fire(event)
            jobs.remove(event.id)
        }
    }

    override fun cancel(eventId: Long) {
        jobs.remove(eventId)?.cancel()
    }

    private fun fire(event: Event) {
        val title = event.title.ifBlank { "Event" }
        val message = "Starts soon"
        val icon = trayIcon
        if (icon != null) {
            icon.displayMessage(title, message, TrayIcon.MessageType.INFO)
        } else {
            println("[Vela Calendar] $title — $message")
        }
    }

    private fun createTrayIcon(): TrayIcon? {
        if (!SystemTray.isSupported()) return null
        return runCatching {
            val image = BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB)
            val tray = TrayIcon(image, "Vela Calendar").apply { isImageAutoSize = true }
            SystemTray.getSystemTray().add(tray)
            tray
        }.getOrNull()
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
        const val ICON_SIZE = 16
    }
}
