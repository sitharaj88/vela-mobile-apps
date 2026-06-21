/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.domain.model

/** How the notes list is ordered (pinned notes always float above the rest regardless of sort). */
enum class NoteSort { Modified, Created, Title }

/** A tag selected as a filter, or null for "all tags". */
data class NoteListQuery(
    val search: String = "",
    val sort: NoteSort = NoteSort.Modified,
    val tag: String? = null,
    val showArchived: Boolean = false,
)

/** True when [note] matches a (already trimmed, case-insensitive) [search] across title/content/items. */
fun noteMatchesSearch(note: Note, search: String): Boolean {
    if (search.isBlank()) return true
    val needle = search.trim().lowercase()
    if (note.title.lowercase().contains(needle)) return true
    if (note.content.lowercase().contains(needle)) return true
    return note.items.any { it.text.lowercase().contains(needle) }
}

/**
 * Applies search + tag + archive filters then sorts, keeping pinned notes first. Pure and
 * deterministic so it can back both the list store and tests.
 */
fun applyNoteQuery(notes: List<Note>, query: NoteListQuery): List<Note> {
    val filtered = notes.filter { note ->
        note.archived == query.showArchived &&
            noteMatchesSearch(note, query.search) &&
            (query.tag == null || query.tag in note.tags)
    }
    val comparator = when (query.sort) {
        NoteSort.Modified -> compareByDescending<Note> { it.updatedAt }
        NoteSort.Created -> compareByDescending<Note> { it.createdAt }
        NoteSort.Title -> compareBy<Note> { it.title.lowercase() }
    }
    return filtered.sortedWith(compareByDescending<Note> { it.pinned }.then(comparator))
}

/** All distinct tags present across [notes], sorted case-insensitively, for filter chips. */
fun collectTags(notes: List<Note>): List<String> =
    notes.flatMap { it.tags }.distinct().sortedBy { it.lowercase() }
