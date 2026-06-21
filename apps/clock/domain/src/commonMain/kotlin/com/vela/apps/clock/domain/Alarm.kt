/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.domain

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

/** Days of the week an alarm may repeat on. Ordinal order is Mon..Sun (matches ISO-8601). */
enum class WeekDay { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }

/**
 * A user-configured alarm.
 *
 * @param hour 0..23 wall-clock hour.
 * @param minute 0..59 wall-clock minute.
 * @param repeatDays empty == fires once (next occurrence); otherwise repeats weekly on these days.
 */
data class Alarm(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val enabled: Boolean = true,
    val repeatDays: PersistentList<WeekDay> = persistentListOf(),
) {
    val isRepeating: Boolean get() = repeatDays.isNotEmpty()
}

/** Stable short labels (Mon, Tue, ...) for chips/summaries, in week order. */
val WeekDay.shortLabel: String
    get() = name.lowercase().replaceFirstChar { it.titlecase() }.take(3)

/** Human summary of the repeat schedule, e.g. "Once", "Every day", "Weekends", "Mon, Wed, Fri". */
fun repeatSummary(days: List<WeekDay>): String {
    if (days.isEmpty()) return "Once"
    val set = days.toSet()
    val weekdays = setOf(
        WeekDay.MONDAY, WeekDay.TUESDAY, WeekDay.WEDNESDAY, WeekDay.THURSDAY, WeekDay.FRIDAY,
    )
    val weekend = setOf(WeekDay.SATURDAY, WeekDay.SUNDAY)
    return when (set) {
        WeekDay.entries.toSet() -> "Every day"
        weekdays -> "Weekdays"
        weekend -> "Weekends"
        else -> WeekDay.entries.filter { it in set }.joinToString(", ") { it.shortLabel }
    }
}

/** Convenience to build an ordered, de-duplicated repeat list from any iterable of days. */
fun normalizeRepeatDays(days: Iterable<WeekDay>): PersistentList<WeekDay> =
    WeekDay.entries.filter { it in days.toSet() }.toPersistentList()
