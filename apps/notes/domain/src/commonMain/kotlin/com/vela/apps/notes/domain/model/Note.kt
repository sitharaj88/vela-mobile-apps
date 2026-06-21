/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.domain.model

import kotlinx.datetime.Instant

/** Whether a note holds free-form text or a checklist of items. */
enum class NoteType { TEXT, CHECKLIST }

/**
 * Accent label a note can be tinted with. [NoteColor.None] means "no label" (default surface).
 * Names mirror a subset of the Vela accent palette so the UI can map them to real colors.
 */
enum class NoteColor { None, Indigo, Teal, Amber, Rose, Forest, Sky, Plum, Crimson }

/** A single checklist entry. [id] is stable within a note so reordering/toggling is unambiguous. */
data class ChecklistItem(
    val id: Long,
    val text: String,
    val checked: Boolean = false,
)

/**
 * A single note. [id] == 0 denotes a not-yet-persisted note.
 *
 * A note is either a [NoteType.TEXT] note (using [content]) or a [NoteType.CHECKLIST] note (using
 * [items]). [pinned] notes float to the top of the list; [archived] notes are hidden from the main
 * list. [color] is an optional accent label and [tags] are simple free-form labels for filtering.
 */
data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val type: NoteType = NoteType.TEXT,
    val items: List<ChecklistItem> = emptyList(),
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val color: NoteColor = NoteColor.None,
    val tags: List<String> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    /** A note is blank when it has no title and no usable body for its [type]. */
    val isBlank: Boolean
        get() = title.isBlank() && when (type) {
            NoteType.TEXT -> content.isBlank()
            NoteType.CHECKLIST -> items.all { it.text.isBlank() }
        }

    /** Count of checked items over total — handy for list previews. */
    val checkedCount: Int get() = items.count { it.checked }
}
