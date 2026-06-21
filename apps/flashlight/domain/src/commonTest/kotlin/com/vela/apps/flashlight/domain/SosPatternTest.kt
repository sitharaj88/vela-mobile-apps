/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SosPatternTest {

    @Test
    fun sos_has_nine_on_pulses() {
        val onPulses = SosPattern.steps().count { it.on }
        assertEquals(9, onPulses, "S-O-S is three dots, three dashes, three dots = 9 lit pulses")
    }

    @Test
    fun on_pulses_follow_dot_dot_dot_dash_dash_dash_dot_dot_dot_timing() {
        val unit = 100L
        val onDurations = SosPattern.steps(unit).filter { it.on }.map { it.durationMillis }
        val expected = listOf(
            unit, unit, unit, // S: dots
            unit * 3, unit * 3, unit * 3, // O: dashes
            unit, unit, unit, // S: dots
        )
        assertEquals(expected, onDurations)
    }

    @Test
    fun pattern_ends_with_an_off_gap_so_it_loops_cleanly() {
        val last = SosPattern.steps().last()
        assertFalse(last.on)
        assertTrue(last.durationMillis > 0L)
    }

    @Test
    fun scaling_the_unit_scales_every_duration() {
        val base = SosPattern.steps(100L).sumOf { it.durationMillis }
        val doubled = SosPattern.steps(200L).sumOf { it.durationMillis }
        assertEquals(base * 2, doubled)
    }
}
