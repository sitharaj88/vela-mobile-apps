/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.domain

/** The field a listing is ordered by. Folders always sort before files regardless of field. */
enum class SortField { NAME, SIZE, DATE }

/** Sort field plus direction. */
data class SortOrder(val field: SortField = SortField.NAME, val ascending: Boolean = true)

/**
 * Pure, platform-independent presentation pipeline for a directory listing. Kept in `domain` so it
 * is unit-testable without any file system: filter hidden, apply a query, then sort.
 */
object FileSorting {

    /** Drops hidden entries unless [showHidden]. */
    fun filterHidden(entries: List<FileNode>, showHidden: Boolean): List<FileNode> =
        if (showHidden) entries else entries.filter { !it.isHidden }

    /** Keeps entries whose name contains [query] (case-insensitive). Blank query keeps everything. */
    fun matchingQuery(entries: List<FileNode>, query: String): List<FileNode> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return entries
        return entries.filter { it.name.contains(trimmed, ignoreCase = true) }
    }

    /** Folders first, then the chosen field/direction; name is the tiebreaker. */
    fun sorted(entries: List<FileNode>, order: SortOrder): List<FileNode> {
        val byField: Comparator<FileNode> = when (order.field) {
            SortField.NAME -> compareBy { it.name.lowercase() }
            SortField.SIZE -> compareBy { it.sizeBytes }
            SortField.DATE -> compareBy { it.lastModified }
        }
        val directed = if (order.ascending) byField else byField.reversed()
        return entries.sortedWith(
            compareByDescending<FileNode> { it.isDirectory }
                .then(directed)
                .thenBy { it.name.lowercase() },
        )
    }

    /** Full pipeline: filter hidden -> filter by query -> sort. */
    fun pipeline(
        entries: List<FileNode>,
        query: String,
        showHidden: Boolean,
        order: SortOrder,
    ): List<FileNode> = sorted(matchingQuery(filterHidden(entries, showHidden), query), order)
}
