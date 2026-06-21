/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.presentation

import app.cash.turbine.test
import com.vela.apps.notes.domain.model.ChecklistItem
import com.vela.apps.notes.domain.model.Note
import com.vela.apps.notes.domain.model.NoteColor
import com.vela.apps.notes.domain.model.NoteType
import com.vela.apps.notes.domain.usecase.DeleteNoteUseCase
import com.vela.apps.notes.domain.usecase.ObserveNoteUseCase
import com.vela.apps.notes.domain.usecase.SaveNoteUseCase
import com.vela.apps.notes.presentation.editor.NoteEditorEffect
import com.vela.apps.notes.presentation.editor.NoteEditorIntent
import com.vela.apps.notes.presentation.editor.NoteEditorStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorStoreTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeNoteRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeNoteRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun store(id: Long = 0, checklist: Boolean = false) = NoteEditorStore(
        noteId = id,
        startAsChecklist = checklist,
        observeNote = ObserveNoteUseCase(repository),
        saveNote = SaveNoteUseCase(repository),
        deleteNote = DeleteNoteUseCase(repository),
    )

    @Test
    fun new_checklist_starts_with_one_empty_item() = runTest(dispatcher) {
        val s = store(checklist = true)
        assertTrue(s.state.value.isChecklist)
        assertEquals(1, s.state.value.items.size)
    }

    @Test
    fun add_remove_and_check_items() = runTest(dispatcher) {
        val s = store(checklist = true)
        val firstId = s.state.value.items.first().id
        s.onIntent(NoteEditorIntent.ItemTextChanged(firstId, "Milk"))
        s.onIntent(NoteEditorIntent.AddItem)
        assertEquals(2, s.state.value.items.size)

        val secondId = s.state.value.items[1].id
        s.onIntent(NoteEditorIntent.ItemCheckedChanged(secondId, true))
        assertTrue(s.state.value.items[1].checked)

        s.onIntent(NoteEditorIntent.RemoveItem(secondId))
        assertEquals(1, s.state.value.items.size)
        assertEquals("Milk", s.state.value.items.first().text)
    }

    @Test
    fun move_item_reorders() = runTest(dispatcher) {
        val s = store(checklist = true)
        val a = s.state.value.items.first().id
        s.onIntent(NoteEditorIntent.ItemTextChanged(a, "A"))
        s.onIntent(NoteEditorIntent.AddItem)
        val b = s.state.value.items[1].id
        s.onIntent(NoteEditorIntent.ItemTextChanged(b, "B"))

        s.onIntent(NoteEditorIntent.MoveItem(from = 0, to = 1))
        assertEquals(listOf("B", "A"), s.state.value.items.map { it.text })
    }

    @Test
    fun toggle_type_seeds_checklist_item() = runTest(dispatcher) {
        val s = store()
        assertFalse(s.state.value.isChecklist)
        s.onIntent(NoteEditorIntent.ToggleType)
        assertTrue(s.state.value.isChecklist)
        assertEquals(1, s.state.value.items.size)
    }

    @Test
    fun close_saves_checklist_dropping_blank_items() = runTest(dispatcher) {
        val s = store(checklist = true)
        val firstId = s.state.value.items.first().id
        s.onIntent(NoteEditorIntent.ItemTextChanged(firstId, "Eggs"))
        s.onIntent(NoteEditorIntent.AddItem) // leave blank
        s.onIntent(NoteEditorIntent.TitleChanged("Shopping"))

        s.effects.test {
            s.onIntent(NoteEditorIntent.Close)
            testScheduler.advanceUntilIdle()
            assertEquals(NoteEditorEffect.NavigateBack, awaitItem())
        }
        val saved = repository.notes.value.single()
        assertEquals(NoteType.CHECKLIST, saved.type)
        assertEquals(listOf("Eggs"), saved.items.map { it.text })
    }

    @Test
    fun close_persists_color_pin_and_tags() = runTest(dispatcher) {
        val s = store()
        s.onIntent(NoteEditorIntent.TitleChanged("Tagged"))
        s.onIntent(NoteEditorIntent.ContentChanged("body"))
        s.onIntent(NoteEditorIntent.ColorChanged(NoteColor.Teal))
        s.onIntent(NoteEditorIntent.TogglePin)
        s.onIntent(NoteEditorIntent.TagsChanged("Work, Ideas"))

        s.onIntent(NoteEditorIntent.Close)
        testScheduler.advanceUntilIdle()

        val saved = repository.notes.value.single()
        assertEquals(NoteColor.Teal, saved.color)
        assertTrue(saved.pinned)
        assertEquals(listOf("work", "ideas"), saved.tags)
    }

    @Test
    fun loads_existing_note_and_preserves_created_at() = runTest(dispatcher) {
        val created = Instant.fromEpochMilliseconds(1234)
        repository.notes.value = listOf(
            Note(
                id = 5,
                title = "Existing",
                content = "hello",
                color = NoteColor.Rose,
                items = listOf(ChecklistItem(id = 1, text = "x")),
                createdAt = created,
                updatedAt = Instant.fromEpochMilliseconds(9999),
            ),
        )
        val s = store(id = 5)
        testScheduler.advanceUntilIdle()
        assertEquals("Existing", s.state.value.title)
        assertEquals(NoteColor.Rose, s.state.value.color)

        s.onIntent(NoteEditorIntent.ContentChanged("hello world"))
        s.onIntent(NoteEditorIntent.Close)
        testScheduler.advanceUntilIdle()
        assertEquals(created, repository.notes.value.single { it.id == 5L }.createdAt)
    }

    @Test
    fun delete_existing_note_removes_it() = runTest(dispatcher) {
        repository.notes.value = listOf(
            Note(id = 8, title = "Doomed", createdAt = Instant.DISTANT_PAST, updatedAt = Instant.DISTANT_PAST),
        )
        val s = store(id = 8)
        testScheduler.advanceUntilIdle()
        s.onIntent(NoteEditorIntent.Delete)
        testScheduler.advanceUntilIdle()
        assertTrue(repository.notes.value.isEmpty())
    }
}
