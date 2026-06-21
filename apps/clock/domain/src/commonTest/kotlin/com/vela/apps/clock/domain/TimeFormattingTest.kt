/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimeFormattingTest {

    @Test
    fun stopwatch_under_an_hour() {
        assertEquals("00:00.00", formatStopwatch(0.milliseconds))
        assertEquals("00:05.23", formatStopwatch(5.seconds + 230.milliseconds))
        assertEquals("01:30.00", formatStopwatch(90.seconds))
    }

    @Test
    fun stopwatch_widens_past_an_hour() {
        assertEquals("1:02:03.00", formatStopwatch(1.hours + 2.minutes + 3.seconds))
    }

    @Test
    fun timer_rounds_up_remaining() {
        assertEquals("00:00", formatTimer(0.milliseconds))
        assertEquals("00:01", formatTimer(400.milliseconds)) // ceil
        assertEquals("05:00", formatTimer(5.minutes))
        assertEquals("1:00:00", formatTimer(1.hours))
    }

    @Test
    fun clock_formats_time_and_date_in_utc() {
        // 2026-06-21T08:09:05Z
        val instant = Instant.parse("2026-06-21T08:09:05Z")
        val display = formatClock(instant, TimeZone.UTC)
        assertEquals("08:09:05", display.time)
        assertEquals("Sun, Jun 21", display.date)
    }
}
