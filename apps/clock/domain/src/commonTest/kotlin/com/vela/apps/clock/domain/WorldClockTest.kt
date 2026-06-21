/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class WorldClockTest {

    private val instant = Instant.parse("2026-06-21T12:00:00Z")

    @Test
    fun city_name_strips_region_and_underscores() {
        assertEquals("New York", cityNameOf("America/New_York"))
        assertEquals("Kolkata", cityNameOf("Asia/Kolkata"))
        assertEquals("UTC", cityNameOf("UTC"))
    }

    @Test
    fun formats_time_in_target_zone() {
        val display = formatWorldClock(instant, TimeZone.of("Asia/Kolkata"), TimeZone.UTC)
        // UTC noon == 17:30 IST.
        assertEquals("17:30", display.time)
        assertEquals("Kolkata", display.cityName)
    }

    @Test
    fun offset_label_handles_half_hour_zones() {
        val display = formatWorldClock(instant, TimeZone.of("Asia/Kolkata"), TimeZone.UTC)
        assertEquals("+5:30", display.offsetLabel)
    }

    @Test
    fun offset_label_whole_hours() {
        val display = formatWorldClock(instant, TimeZone.of("America/New_York"), TimeZone.UTC)
        // EDT in June is UTC-4.
        assertEquals("-4h", display.offsetLabel)
    }

    @Test
    fun same_zone_reports_same() {
        val display = formatWorldClock(instant, TimeZone.UTC, TimeZone.UTC)
        assertEquals("Same", display.offsetLabel)
        assertEquals("", display.dayLabel)
    }

    @Test
    fun day_label_shows_tomorrow_across_date_line() {
        // 23:00 UTC -> Tokyo is already next day (08:00).
        val late = Instant.parse("2026-06-21T23:00:00Z")
        val display = formatWorldClock(late, TimeZone.of("Asia/Tokyo"), TimeZone.UTC)
        assertEquals("Tomorrow", display.dayLabel)
    }
}

class AlarmSummaryTest {

    @Test
    fun once_when_no_days() {
        assertEquals("Once", repeatSummary(emptyList()))
    }

    @Test
    fun every_day() {
        assertEquals("Every day", repeatSummary(WeekDay.entries))
    }

    @Test
    fun weekdays_and_weekends() {
        assertEquals(
            "Weekdays",
            repeatSummary(
                listOf(
                    WeekDay.MONDAY, WeekDay.TUESDAY, WeekDay.WEDNESDAY, WeekDay.THURSDAY, WeekDay.FRIDAY,
                ),
            ),
        )
        assertEquals("Weekends", repeatSummary(listOf(WeekDay.SATURDAY, WeekDay.SUNDAY)))
    }

    @Test
    fun specific_days_listed_in_week_order() {
        assertEquals(
            "Mon, Wed, Fri",
            repeatSummary(listOf(WeekDay.FRIDAY, WeekDay.MONDAY, WeekDay.WEDNESDAY)),
        )
    }
}
