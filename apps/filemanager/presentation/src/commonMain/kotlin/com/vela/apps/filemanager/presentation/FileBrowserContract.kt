/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.presentation

import com.vela.apps.filemanager.domain.FileNode
import com.vela.apps.filemanager.domain.FileSorting
import com.vela.apps.filemanager.domain.SortOrder

/** A clickable segment of the breadcrumb path bar. */
data class Crumb(val name: String, val path: String)

/**
 * Immutable browser state. [entries] is the raw listing; [visibleEntries] applies the show-hidden
 * toggle, search query, and sort order so the UI renders a single derived list.
 */
data class FileBrowserState(
    val currentPath: String = "",
    val entries: List<FileNode> = emptyList(),
    val roots: List<FileNode> = emptyList(),
    val query: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<FileNode> = emptyList(),
    val showHidden: Boolean = false,
    val sortOrder: SortOrder = SortOrder(),
    val selection: Set<String> = emptySet(),
    val canGoUp: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val inSelectionMode: Boolean get() = selection.isNotEmpty()

    /** Derived, ordered list the UI shows (directory listing or live search results). */
    val visibleEntries: List<FileNode>
        get() = if (isSearching) {
            FileSorting.sorted(FileSorting.filterHidden(searchResults, showHidden), sortOrder)
        } else {
            FileSorting.pipeline(entries, query, showHidden, sortOrder)
        }

    /** Breadcrumb segments split from [currentPath]. */
    val crumbs: List<Crumb> get() = breadcrumbsOf(currentPath)

    val totalSizeBytes: Long get() = entries.filter { !it.isDirectory }.sumOf { it.sizeBytes }
    val folderCount: Int get() = entries.count { it.isDirectory }
    val fileCount: Int get() = entries.count { !it.isDirectory }
}

/** Splits an absolute path into clickable breadcrumb segments, handling both `/` and `\` roots. */
@Suppress("CyclomaticComplexMethod")
internal fun breadcrumbsOf(path: String): List<Crumb> {
    if (path.isEmpty()) return emptyList()
    val separator = if (path.contains('\\')) '\\' else '/'
    val isUnixRoot = path.startsWith(separator)
    val result = mutableListOf<Crumb>()
    val builder = StringBuilder()
    path.split(separator).forEachIndexed { index, part ->
        if (part.isEmpty()) {
            if (index == 0 && isUnixRoot) result += Crumb(separator.toString(), separator.toString())
            return@forEachIndexed
        }
        when {
            builder.isNotEmpty() && builder.last() != separator -> builder.append(separator)
            builder.isEmpty() && isUnixRoot -> builder.append(separator)
        }
        builder.append(part)
        result += Crumb(part, builder.toString())
    }
    return result
}

sealed interface FileBrowserIntent {
    data class Open(val path: String) : FileBrowserIntent
    data object GoUp : FileBrowserIntent
    data object Refresh : FileBrowserIntent

    data class QueryChanged(val query: String) : FileBrowserIntent
    data object ClearSearch : FileBrowserIntent
    data class SortChanged(val order: SortOrder) : FileBrowserIntent
    data object ToggleHidden : FileBrowserIntent

    data class ToggleSelect(val path: String) : FileBrowserIntent
    data object ClearSelection : FileBrowserIntent
    data object SelectAll : FileBrowserIntent

    data class CreateFolder(val name: String) : FileBrowserIntent
    data class Rename(val path: String, val newName: String) : FileBrowserIntent
    data class Delete(val paths: Set<String>) : FileBrowserIntent
    data class Copy(val paths: Set<String>, val targetDir: String) : FileBrowserIntent
    data class Move(val paths: Set<String>, val targetDir: String) : FileBrowserIntent
}

sealed interface FileBrowserEffect {
    data class ShowMessage(val text: String) : FileBrowserEffect
    data class ShowError(val text: String) : FileBrowserEffect
}
