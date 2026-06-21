/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.presentation

import com.vela.apps.notes.domain.model.Note
import com.vela.apps.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [NoteRepository] for store tests. Emits the raw, unfiltered list (filtering is domain). */
class FakeNoteRepository(initial: List<Note> = emptyList()) : NoteRepository {

    val notes = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    override fun observeNotes(): Flow<List<Note>> = notes

    override fun observeNote(id: Long): Flow<Note?> =
        notes.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun save(note: Note): Long {
        return if (note.id == 0L) {
            val id = nextId++
            notes.value = notes.value + note.copy(id = id)
            id
        } else {
            notes.value = notes.value.map { if (it.id == note.id) note else it }
            note.id
        }
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) {
        notes.value = notes.value.map { if (it.id == id) it.copy(pinned = pinned) else it }
    }

    override suspend fun setArchived(id: Long, archived: Boolean) {
        notes.value = notes.value.map { if (it.id == id) it.copy(archived = archived) else it }
    }

    override suspend fun delete(id: Long) {
        notes.value = notes.value.filterNot { it.id == id }
    }
}
