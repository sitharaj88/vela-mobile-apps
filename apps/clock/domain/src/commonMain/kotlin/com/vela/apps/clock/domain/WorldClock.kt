/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue

/** A timezone the user has pinned to the World Clock screen. */
data class WorldClockCity(
    val zoneId: String,
)

/** Rendered World-Clock row: city name, its current `HH:mm`, and the offset vs. the device. */
data class WorldClockDisplay(
    val zoneId: String,
    val cityName: String,
    val time: String,
    val dayLabel: String,
    val offsetLabel: String,
)

private fun Int.pad2(): String = toString().padStart(2, '0')

/** Derives a friendly city/region name from an IANA zone id, e.g. `America/New_York` -> `New York`. */
fun cityNameOf(zoneId: String): String =
    zoneId.substringAfterLast('/').replace('_', ' ')

/**
 * Formats the current time in [zone] relative to [deviceZone].
 *
 * [dayLabel] is "" when the zone's date matches the device's, otherwise "Tomorrow"/"Yesterday".
 * [offsetLabel] expresses the difference from the device clock, e.g. "+5:30", "-3h", "Same".
 */
fun formatWorldClock(instant: Instant, zone: TimeZone, deviceZone: TimeZone): WorldClockDisplay {
    val there = instant.toLocalDateTime(zone)
    val here = instant.toLocalDateTime(deviceZone)
    val time = "${there.hour.pad2()}:${there.minute.pad2()}"

    val dayLabel = when {
        there.date > here.date -> "Tomorrow"
        there.date < here.date -> "Yesterday"
        else -> ""
    }

    val offsetMinutes = (zone.offsetMinutesAt(instant) - deviceZone.offsetMinutesAt(instant))
    return WorldClockDisplay(
        zoneId = zone.id,
        cityName = cityNameOf(zone.id),
        time = time,
        dayLabel = dayLabel,
        offsetLabel = formatOffsetLabel(offsetMinutes),
    )
}

private fun formatOffsetLabel(offsetMinutes: Int): String {
    if (offsetMinutes == 0) return "Same"
    val sign = if (offsetMinutes > 0) "+" else "-"
    val abs = offsetMinutes.absoluteValue
    val hours = abs / MINUTES_PER_HOUR_OFFSET
    val minutes = abs % MINUTES_PER_HOUR_OFFSET
    return if (minutes == 0) "${sign}${hours}h" else "$sign$hours:${minutes.pad2()}"
}

private const val MINUTES_PER_HOUR_OFFSET = 60
private const val SECONDS_PER_MINUTE_OFFSET = 60

private fun TimeZone.offsetMinutesAt(instant: Instant): Int =
    instant.offsetIn(this).totalSeconds / SECONDS_PER_MINUTE_OFFSET
