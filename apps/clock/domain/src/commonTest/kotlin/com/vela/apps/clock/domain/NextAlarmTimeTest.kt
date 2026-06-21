/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.domain

import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NextAlarmTimeTest {

    private val utc = TimeZone.UTC

    // Reference "now": Sunday 2026-06-21T08:00:00Z.
    private val now = Instant.parse("2026-06-21T08:00:00Z")

    @Test
    fun disabled_alarm_has_no_next_time() {
        val alarm = Alarm(hour = 9, minute = 0, enabled = false)
        assertNull(nextAlarmTime(alarm, now, utc))
    }

    @Test
    fun one_shot_later_today_fires_today() {
        val alarm = Alarm(hour = 9, minute = 30)
        val next = nextAlarmTime(alarm, now, utc)
        assertEquals(Instant.parse("2026-06-21T09:30:00Z"), next)
    }

    @Test
    fun one_shot_earlier_today_rolls_to_tomorrow() {
        val alarm = Alarm(hour = 7, minute = 0)
        val next = nextAlarmTime(alarm, now, utc)
        assertEquals(Instant.parse("2026-06-22T07:00:00Z"), next)
    }

    @Test
    fun one_shot_exactly_now_rolls_forward() {
        val alarm = Alarm(hour = 8, minute = 0)
        val next = nextAlarmTime(alarm, now, utc)
        // 08:00 today is not strictly after now, so next is 08:00 tomorrow.
        assertEquals(Instant.parse("2026-06-22T08:00:00Z"), next)
    }

    @Test
    fun repeating_picks_nearest_future_day() {
        // now is Sunday. Repeat Mon + Wed at 06:00 -> next is Monday 2026-06-22.
        val alarm = Alarm(
            hour = 6,
            minute = 0,
            repeatDays = persistentListOf(WeekDay.MONDAY, WeekDay.WEDNESDAY),
        )
        val next = nextAlarmTime(alarm, now, utc)
        assertEquals(Instant.parse("2026-06-22T06:00:00Z"), next)
    }

    @Test
    fun repeating_today_but_time_passed_skips_to_next_repeat_day() {
        // now Sunday 08:00. Repeat Sun only at 07:00 already passed -> next Sunday.
        val alarm = Alarm(hour = 7, minute = 0, repeatDays = persistentListOf(WeekDay.SUNDAY))
        val next = nextAlarmTime(alarm, now, utc)
        assertEquals(Instant.parse("2026-06-28T07:00:00Z"), next)
    }

    @Test
    fun repeating_today_and_time_in_future_fires_today() {
        val alarm = Alarm(hour = 23, minute = 0, repeatDays = persistentListOf(WeekDay.SUNDAY))
        val next = nextAlarmTime(alarm, now, utc)
        assertEquals(Instant.parse("2026-06-21T23:00:00Z"), next)
    }

    @Test
    fun next_time_is_always_strictly_after_now() {
        val alarm = Alarm(hour = 8, minute = 0, repeatDays = persistentListOf(WeekDay.SUNDAY))
        val next = nextAlarmTime(alarm, now, utc)
        assertTrue(next!! > now)
    }
}
