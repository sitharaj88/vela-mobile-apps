/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.domain

import com.vela.apps.notes.domain.model.ChecklistCodec
import com.vela.apps.notes.domain.model.ChecklistItem
import com.vela.apps.notes.domain.model.TagCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChecklistCodecTest {

    @Test
    fun encode_decode_round_trips_items_with_state() {
        val items = listOf(
            ChecklistItem(id = 1, text = "Buy milk", checked = false),
            ChecklistItem(id = 2, text = "Call Ada", checked = true),
        )
        val decoded = ChecklistCodec.decode(ChecklistCodec.encode(items))
        assertEquals(items, decoded)
    }

    @Test
    fun empty_list_round_trips_to_empty() {
        assertEquals(emptyList(), ChecklistCodec.decode(ChecklistCodec.encode(emptyList())))
        assertEquals(emptyList(), ChecklistCodec.decode(""))
    }

    @Test
    fun item_text_with_delimiters_and_newlines_survives() {
        val items = listOf(ChecklistItem(id = 7, text = "weird\ntext\twith chars", checked = true))
        val decoded = ChecklistCodec.decode(ChecklistCodec.encode(items))
        // Control chars are sanitized to spaces but the item count/state is preserved.
        assertEquals(1, decoded.size)
        assertEquals(7, decoded.first().id)
        assertTrue(decoded.first().checked)
    }

    @Test
    fun decode_skips_malformed_records() {
        assertEquals(emptyList(), ChecklistCodec.decode("garbage-without-fields"))
    }

    @Test
    fun tags_are_normalized_lowercased_trimmed_deduped() {
        assertEquals(listOf("work", "home"), TagCodec.decode("Work, HOME ,  work ,"))
        assertEquals("work,home", TagCodec.encode(listOf(" Work ", "home", "WORK")))
    }
}
