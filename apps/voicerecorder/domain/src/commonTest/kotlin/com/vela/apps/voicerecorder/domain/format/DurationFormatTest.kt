/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.domain.format

import kotlin.test.Test
import kotlin.test.assertEquals

class DurationFormatTest {

    @Test
    fun formats_sub_minute_durations_as_m_ss() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:05", formatDuration(5_000))
        assertEquals("0:42", formatDuration(42_000))
    }

    @Test
    fun pads_seconds_to_two_digits() {
        assertEquals("1:09", formatDuration(69_000))
        assertEquals("2:00", formatDuration(120_000))
    }

    @Test
    fun formats_hour_plus_durations_as_h_mm_ss() {
        assertEquals("1:00:00", formatDuration(3_600_000))
        assertEquals("1:02:03", formatDuration(3_600_000 + 120_000 + 3_000))
    }

    @Test
    fun negative_durations_clamp_to_zero() {
        assertEquals("0:00", formatDuration(-5_000))
    }

    @Test
    fun formats_sizes_in_bytes_kb_and_mb() {
        assertEquals("512 B", formatSize(512))
        assertEquals("1.0 KB", formatSize(1_024))
        assertEquals("1.5 KB", formatSize(1_536))
        assertEquals("1.0 MB", formatSize(1_048_576))
    }

    @Test
    fun negative_sizes_clamp_to_zero_bytes() {
        assertEquals("0 B", formatSize(-1))
    }
}
