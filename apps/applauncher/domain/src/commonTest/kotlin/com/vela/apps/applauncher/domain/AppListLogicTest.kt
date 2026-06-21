/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class AppListLogicTest {

    private val apps = listOf(
        AppEntry(id = "c", label = "Calculator"),
        AppEntry(id = "a", label = "browser"),
        AppEntry(id = "b", label = "Browser Beta"),
        AppEntry(id = "d", label = "Zen"),
    )

    @Test
    fun sort_is_alphabetical_case_insensitive_with_id_tiebreak() {
        val sorted = AppListLogic.sort(apps).map { it.id }
        assertEquals(listOf("a", "b", "c", "d"), sorted)
    }

    @Test
    fun filter_matches_label_case_insensitively() {
        val result = AppListLogic.filter(apps, "brow").map { it.id }
        assertEquals(listOf("a", "b"), result)
    }

    @Test
    fun filter_blank_query_returns_everything() {
        assertEquals(apps, AppListLogic.filter(apps, "   "))
    }

    @Test
    fun filter_no_match_returns_empty() {
        assertEquals(emptyList(), AppListLogic.filter(apps, "xyz"))
    }

    @Test
    fun favorites_keeps_only_starred_in_input_order() {
        val sorted = AppListLogic.sort(apps)
        val result = AppListLogic.favorites(sorted, setOf("c", "a")).map { it.id }
        assertEquals(listOf("a", "c"), result)
    }

    @Test
    fun toggle_adds_then_removes() {
        val once = AppListLogic.toggleFavorite(emptySet(), "a")
        assertEquals(setOf("a"), once)
        val twice = AppListLogic.toggleFavorite(once, "a")
        assertEquals(emptySet(), twice)
    }
}
