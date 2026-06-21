/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.presentation.editor

import com.vela.apps.notes.domain.model.ChecklistItem
import com.vela.apps.notes.domain.model.NoteColor
import com.vela.apps.notes.domain.model.NoteType
import kotlinx.datetime.Instant

data class NoteEditorState(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val type: NoteType = NoteType.TEXT,
    val items: List<ChecklistItem> = emptyList(),
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val color: NoteColor = NoteColor.None,
    val tags: List<String> = emptyList(),
    /** When the note was first created; null for a new note (stamped on first save). */
    val createdAt: Instant? = null,
) {
    val isNew: Boolean get() = id == 0L
    val isChecklist: Boolean get() = type == NoteType.CHECKLIST
}

sealed interface NoteEditorIntent {
    data class TitleChanged(val title: String) : NoteEditorIntent
    data class ContentChanged(val content: String) : NoteEditorIntent
    data object ToggleType : NoteEditorIntent

    data class ItemTextChanged(val id: Long, val text: String) : NoteEditorIntent
    data class ItemCheckedChanged(val id: Long, val checked: Boolean) : NoteEditorIntent
    data object AddItem : NoteEditorIntent
    data class RemoveItem(val id: Long) : NoteEditorIntent
    data class MoveItem(val from: Int, val to: Int) : NoteEditorIntent

    data class ColorChanged(val color: NoteColor) : NoteEditorIntent
    data object TogglePin : NoteEditorIntent
    data object ToggleArchive : NoteEditorIntent
    data class TagsChanged(val raw: String) : NoteEditorIntent

    data object Delete : NoteEditorIntent

    /** User is leaving the editor; the note is auto-saved (or discarded if blank). */
    data object Close : NoteEditorIntent
}

sealed interface NoteEditorEffect {
    data object NavigateBack : NoteEditorEffect
}
