/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.domain

import com.vela.apps.notes.domain.model.ChecklistItem
import com.vela.apps.notes.domain.model.Note
import com.vela.apps.notes.domain.model.NoteType
import com.vela.apps.notes.domain.repository.NoteRepository
import com.vela.apps.notes.domain.usecase.SaveNoteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SaveNoteUseCaseTest {

    private class FakeRepo : NoteRepository {
        var saved: Note? = null
        var deletedId: Long? = null
        override fun observeNotes(): Flow<List<Note>> = flowOf(emptyList())
        override fun observeNote(id: Long): Flow<Note?> = flowOf(null)
        override suspend fun save(note: Note): Long {
            saved = note
            return 42
        }
        override suspend fun setPinned(id: Long, pinned: Boolean) = Unit
        override suspend fun setArchived(id: Long, archived: Boolean) = Unit
        override suspend fun delete(id: Long) { deletedId = id }
    }

    private fun note(id: Long = 0, title: String = "", content: String = "") = Note(
        id = id,
        title = title,
        content = content,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now(),
    )

    @Test
    fun saves_non_blank_note_and_returns_id() = runTest {
        val repo = FakeRepo()
        val id = SaveNoteUseCase(repo)(note(title = "Hello"))
        assertEquals(42, id)
        assertEquals("Hello", repo.saved?.title)
    }

    @Test
    fun blank_new_note_is_discarded_not_saved() = runTest {
        val repo = FakeRepo()
        val id = SaveNoteUseCase(repo)(note(id = 0, title = "  ", content = ""))
        assertNull(id)
        assertNull(repo.saved)
        assertNull(repo.deletedId)
    }

    @Test
    fun existing_note_emptied_to_blank_is_deleted() = runTest {
        val repo = FakeRepo()
        val id = SaveNoteUseCase(repo)(note(id = 7, title = "", content = ""))
        assertNull(id)
        assertEquals(7, repo.deletedId)
        assertNull(repo.saved)
    }

    @Test
    fun save_stamps_updated_time() = runTest {
        val repo = FakeRepo()
        SaveNoteUseCase(repo)(note(title = "x"))
        assertTrue(repo.saved!!.updatedAt.toEpochMilliseconds() > 0)
    }

    @Test
    fun checklist_with_only_unchecked_text_is_not_blank() = runTest {
        val repo = FakeRepo()
        val checklist = note(id = 3).copy(
            type = NoteType.CHECKLIST,
            items = listOf(ChecklistItem(id = 1, text = "Milk")),
        )
        val id = SaveNoteUseCase(repo)(checklist)
        assertEquals(42, id)
        assertNull(repo.deletedId)
    }

    @Test
    fun checklist_with_only_empty_items_is_blank_and_deleted() = runTest {
        val repo = FakeRepo()
        val checklist = note(id = 9).copy(
            type = NoteType.CHECKLIST,
            items = listOf(ChecklistItem(id = 1, text = "  ")),
        )
        val id = SaveNoteUseCase(repo)(checklist)
        assertNull(id)
        assertEquals(9, repo.deletedId)
    }
}
