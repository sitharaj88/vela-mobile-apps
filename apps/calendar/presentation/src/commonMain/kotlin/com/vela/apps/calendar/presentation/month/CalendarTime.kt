/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.presentation.month

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** Epoch-millis at the start of [date] in [zone]. */
fun startOfDayMillis(date: LocalDate, zone: TimeZone = TimeZone.currentSystemDefault()): Long =
    date.atStartOfDayIn(zone).toEpochMilliseconds()

/** Epoch-millis at the start of the day *after* [date] in [zone] (exclusive day end). */
fun endOfDayExclusiveMillis(date: LocalDate, zone: TimeZone = TimeZone.currentSystemDefault()): Long =
    date.plusDays(1).atStartOfDayIn(zone).toEpochMilliseconds()

/** Epoch-millis for [date] at [time] in [zone]. */
fun millisAt(
    date: LocalDate,
    time: LocalTime,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): Long = LocalDateTime(date, time).toInstant(zone).toEpochMilliseconds()

/** The [LocalDateTime] for [millis] in [zone]. */
fun localDateTimeOf(millis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone)

/** The [LocalDate] for [millis] in [zone]. */
fun localDateOf(millis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    localDateTimeOf(millis, zone).date

private fun LocalDate.plusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(toEpochDays() + days)
