/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.domain

/**
 * Pure, platform-independent list logic shared by the store and exercised directly by unit tests.
 * Keeping it free of coroutines/Compose makes the search/sort/favorite rules trivial to verify.
 */
object AppListLogic {

    /** Sorts apps alphabetically by label, case-insensitively, with a stable id tie-break. */
    fun sort(apps: List<AppEntry>): List<AppEntry> =
        apps.sortedWith(compareBy({ it.label.lowercase() }, { it.id }))

    /** Filters [apps] to those whose label contains the trimmed [query] (case-insensitive). */
    fun filter(apps: List<AppEntry>, query: String): List<AppEntry> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return apps
        return apps.filter { it.label.contains(trimmed, ignoreCase = true) }
    }

    /** Returns the subset of [apps] whose id is in [favoriteIds], preserving the sorted order. */
    fun favorites(apps: List<AppEntry>, favoriteIds: Set<String>): List<AppEntry> =
        apps.filter { it.id in favoriteIds }

    /** Toggles [id] within [current]: removes it if present, otherwise adds it. */
    fun toggleFavorite(current: Set<String>, id: String): Set<String> =
        if (id in current) current - id else current + id
}
