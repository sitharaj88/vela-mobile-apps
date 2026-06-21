/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.domain

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days as durationDays

private const val DAYS_IN_WEEK = 7

private fun DayOfWeek.toWeekDay(): WeekDay = when (this) {
    DayOfWeek.MONDAY -> WeekDay.MONDAY
    DayOfWeek.TUESDAY -> WeekDay.TUESDAY
    DayOfWeek.WEDNESDAY -> WeekDay.WEDNESDAY
    DayOfWeek.THURSDAY -> WeekDay.THURSDAY
    DayOfWeek.FRIDAY -> WeekDay.FRIDAY
    DayOfWeek.SATURDAY -> WeekDay.SATURDAY
    else -> WeekDay.SUNDAY
}

/**
 * Computes the next [Instant] at which [alarm] should fire, strictly after [now], in [timeZone].
 *
 * - A one-shot alarm (no repeat days) fires at the next occurrence of its hour:minute (today if still
 *   in the future, otherwise tomorrow).
 * - A repeating alarm fires at the nearest future occurrence among its [Alarm.repeatDays].
 *
 * Returns `null` for a disabled alarm.
 */
fun nextAlarmTime(alarm: Alarm, now: Instant, timeZone: TimeZone): Instant? {
    if (!alarm.enabled) return null
    val target = LocalTime(alarm.hour, alarm.minute)
    val localNow: LocalDateTime = now.toLocalDateTime(timeZone)
    val today: LocalDate = localNow.date

    if (!alarm.isRepeating) {
        val candidate = today.atTime(target)
        val firesToday = candidate.toInstant(timeZone) > now
        val date = if (firesToday) today else today.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
        return date.atTime(target).toInstant(timeZone)
    }

    val repeat = alarm.repeatDays.toSet()
    for (offset in 0..DAYS_IN_WEEK) {
        val date = today.plus(offset, kotlinx.datetime.DateTimeUnit.DAY)
        if (date.dayOfWeek.toWeekDay() !in repeat) continue
        val candidate = date.atTime(target).toInstant(timeZone)
        if (candidate > now) return candidate
    }
    // Should be unreachable for a non-empty repeat set, but fall back to one week out.
    return today.atTime(target).toInstant(timeZone).plus(DAYS_IN_WEEK.durationDays)
}
