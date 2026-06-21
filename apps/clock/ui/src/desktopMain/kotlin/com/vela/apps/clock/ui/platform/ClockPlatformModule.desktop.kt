/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.platform

import com.vela.apps.clock.domain.Alarm
import com.vela.apps.clock.domain.AlarmScheduler
import com.vela.apps.clock.domain.nextAlarmTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap

actual fun clockPlatformModule(): Module = module {
    single { DesktopAlarmScheduler() } bind AlarmScheduler::class
}

/**
 * Real in-process scheduler. For each enabled alarm it launches a coroutine that sleeps until the
 * computed next fire time and then shows a desktop notification (SystemTray when supported, else a
 * console log). Fires repeatedly for repeating alarms while the app is running.
 */
class DesktopAlarmScheduler(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) : AlarmScheduler {

    private val jobs = ConcurrentHashMap<Long, Job>()
    private val trayIcon: TrayIcon? by lazy(::createTrayIcon)

    override fun schedule(alarm: Alarm) {
        cancel(alarm.id)
        if (!alarm.enabled) return
        jobs[alarm.id] = scope.launch {
            while (isActive) {
                val now = Clock.System.now()
                val next = nextAlarmTime(alarm, now, TimeZone.currentSystemDefault()) ?: break
                val waitMs = (next - now).inWholeMilliseconds
                if (waitMs > 0) delay(waitMs)
                fire(alarm)
                if (!alarm.isRepeating) break
                // Nudge past the boundary so the next computation rolls to the following occurrence.
                delay(BOUNDARY_NUDGE_MS)
            }
        }
    }

    override fun cancel(alarmId: Long) {
        jobs.remove(alarmId)?.cancel()
    }

    private fun fire(alarm: Alarm) {
        val title = "Vela Alarm"
        val message = alarm.label.ifBlank { "Alarm" } +
            " — ${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')}"
        val icon = trayIcon
        if (icon != null) {
            icon.displayMessage(title, message, TrayIcon.MessageType.INFO)
        } else {
            println("[$title] $message")
        }
    }

    private fun createTrayIcon(): TrayIcon? {
        if (!SystemTray.isSupported()) return null
        return runCatching {
            val image = BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB)
            val tray = TrayIcon(image, "Vela Clock").apply { isImageAutoSize = true }
            SystemTray.getSystemTray().add(tray)
            tray
        }.getOrNull()
    }

    private companion object {
        const val BOUNDARY_NUDGE_MS = 1000L
        const val ICON_SIZE = 16
    }
}
