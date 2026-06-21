/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.domain.usecase

import com.vela.apps.notes.domain.model.Note
import com.vela.apps.notes.domain.model.NoteListQuery
import com.vela.apps.notes.domain.model.applyNoteQuery
import com.vela.apps.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * Observe notes, applying a [NoteListQuery] (search + sort + tag/archive filters, pinned-first) on
 * top of the raw repository stream. Filtering lives here so it is pure and unit-testable.
 */
class ObserveNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(query: NoteListQuery): Flow<List<Note>> =
        repository.observeNotes().map { notes -> applyNoteQuery(notes, query) }
}

/** Observe the raw, unfiltered note list — used to derive the set of available tags for filtering. */
class ObserveAllNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.observeNotes()
}

/** Observe a single note by id (null while it doesn't exist yet). */
class ObserveNoteUseCase(private val repository: NoteRepository) {
    operator fun invoke(id: Long): Flow<Note?> = repository.observeNote(id)
}

/**
 * Persist a note. Blank notes are not saved (and an existing note emptied to blank is deleted),
 * mirroring the "don't keep empty notes" behavior users expect. Returns the surviving id, or null
 * if the note was discarded/deleted.
 */
class SaveNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note): Long? {
        if (note.isBlank) {
            if (note.id != 0L) repository.delete(note.id)
            return null
        }
        return repository.save(note.copy(updatedAt = Clock.System.now()))
    }
}

/** Pin or unpin a note so it floats to the top of the list. */
class SetPinnedUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long, pinned: Boolean) = repository.setPinned(id, pinned)
}

/** Archive or unarchive a note, moving it out of / back into the main list. */
class SetArchivedUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long, archived: Boolean) = repository.setArchived(id, archived)
}

class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
