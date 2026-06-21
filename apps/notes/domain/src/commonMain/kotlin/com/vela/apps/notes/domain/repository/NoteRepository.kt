/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.domain.repository

import com.vela.apps.notes.domain.model.Note
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for notes. Implemented by the data layer (Room). */
interface NoteRepository {
    /**
     * All notes (both active and archived), newest-updated first. Search, sort, pin-ordering and
     * archive filtering are applied in the domain/presentation layer so the policy is testable and
     * platform-independent.
     */
    fun observeNotes(): Flow<List<Note>>

    fun observeNote(id: Long): Flow<Note?>

    /** Inserts or updates; returns the note id. */
    suspend fun save(note: Note): Long

    suspend fun setPinned(id: Long, pinned: Boolean)

    suspend fun setArchived(id: Long, archived: Boolean)

    suspend fun delete(id: Long)
}
