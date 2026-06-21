/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.presentation.list

import com.vela.apps.notes.domain.model.Note
import com.vela.apps.notes.domain.model.NoteSort

/** How the list renders each note. */
enum class NotesLayout { List, Grid }

data class NotesListState(
    val query: String = "",
    val sort: NoteSort = NoteSort.Modified,
    val selectedTag: String? = null,
    val showArchived: Boolean = false,
    val layout: NotesLayout = NotesLayout.List,
    val notes: List<Note> = emptyList(),
    val tags: List<String> = emptyList(),
    val isLoading: Boolean = true,
) {
    val isEmpty: Boolean get() = !isLoading && notes.isEmpty()
    val isFiltering: Boolean get() = query.isNotBlank() || selectedTag != null
}

sealed interface NotesListIntent {
    data class QueryChanged(val query: String) : NotesListIntent
    data class SortChanged(val sort: NoteSort) : NotesListIntent
    data class TagSelected(val tag: String?) : NotesListIntent
    data object ToggleArchivedView : NotesListIntent
    data object ToggleLayout : NotesListIntent
    data class TogglePin(val id: Long, val pinned: Boolean) : NotesListIntent
    data class ToggleArchive(val id: Long, val archived: Boolean) : NotesListIntent
    data class OpenNote(val id: Long) : NotesListIntent
    data object CreateNote : NotesListIntent
    data object CreateChecklist : NotesListIntent
}

sealed interface NotesListEffect {
    /** [checklist] starts the new note in checklist mode. */
    data class OpenEditor(val noteId: Long, val checklist: Boolean = false) : NotesListEffect
}
