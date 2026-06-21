/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.data

import com.vela.apps.clock.domain.Alarm
import com.vela.apps.clock.domain.WeekDay
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepeatDaysCodecTest {

    @Test
    fun encodes_ordinals_in_order() {
        assertEquals("0,2,4", encodeRepeatDays(listOf(WeekDay.MONDAY, WeekDay.WEDNESDAY, WeekDay.FRIDAY)))
    }

    @Test
    fun empty_list_encodes_to_blank() {
        assertEquals("", encodeRepeatDays(emptyList()))
    }

    @Test
    fun decodes_blank_to_empty() {
        assertTrue(decodeRepeatDays("").isEmpty())
    }

    @Test
    fun decode_sorts_dedupes_and_drops_garbage() {
        val decoded = decodeRepeatDays("4,0,0,99,abc,2")
        assertEquals(persistentListOf(WeekDay.MONDAY, WeekDay.WEDNESDAY, WeekDay.FRIDAY), decoded)
    }

    @Test
    fun entity_roundtrip_preserves_alarm() {
        val alarm = Alarm(
            id = 7,
            hour = 6,
            minute = 45,
            label = "Gym",
            enabled = false,
            repeatDays = persistentListOf(WeekDay.TUESDAY, WeekDay.THURSDAY),
        )
        assertEquals(alarm, alarm.toEntity().toDomain())
    }
}
