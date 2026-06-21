/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.presentation

import app.cash.turbine.test
import com.vela.apps.notes.domain.model.Note
import com.vela.apps.notes.domain.model.NoteSort
import com.vela.apps.notes.domain.usecase.ObserveAllNotesUseCase
import com.vela.apps.notes.domain.usecase.ObserveNotesUseCase
import com.vela.apps.notes.domain.usecase.SetArchivedUseCase
import com.vela.apps.notes.domain.usecase.SetPinnedUseCase
import com.vela.apps.notes.presentation.list.NotesLayout
import com.vela.apps.notes.presentation.list.NotesListEffect
import com.vela.apps.notes.presentation.list.NotesListIntent
import com.vela.apps.notes.presentation.list.NotesListStore
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NotesListStoreTest {

    private val dispatcher = StandardTestDispatcher()

    private fun note(
        id: Long,
        title: String,
        updated: Long,
        pinned: Boolean = false,
        archived: Boolean = false,
        tags: List<String> = emptyList(),
    ) = Note(
        id = id,
        title = title,
        pinned = pinned,
        archived = archived,
        tags = tags,
        createdAt = Instant.fromEpochMilliseconds(updated),
        updatedAt = Instant.fromEpochMilliseconds(updated),
    )

    private lateinit var repository: FakeNoteRepository
    private lateinit var store: NotesListStore

    private fun newStore() = NotesListStore(
        observeNotes = ObserveNotesUseCase(repository),
        observeAllNotes = ObserveAllNotesUseCase(repository),
        setPinned = SetPinnedUseCase(repository),
        setArchived = SetArchivedUseCase(repository),
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeNoteRepository(
            initial = listOf(
                note(1, "Alpha", updated = 100, tags = listOf("work")),
                note(2, "Beta", updated = 300, pinned = true),
                note(3, "Gamma", updated = 200, archived = true),
            ),
        )
        store = newStore()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loads_active_notes_pinned_first() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        val state = store.state.value
        assertEquals(false, state.isLoading)
        // Beta is pinned -> first; Gamma is archived -> excluded.
        assertEquals(listOf(2L, 1L), state.notes.map { it.id })
    }

    @Test
    fun derives_available_tags() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("work"), store.state.value.tags)
    }

    @Test
    fun search_filters_notes() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(NotesListIntent.QueryChanged("alpha"))
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(1L), store.state.value.notes.map { it.id })
    }

    @Test
    fun sort_by_title_reorders() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(NotesListIntent.SortChanged(NoteSort.Title))
        testScheduler.advanceUntilIdle()
        // Pinned Beta still first, then Alpha by title.
        assertEquals(listOf(2L, 1L), store.state.value.notes.map { it.id })
    }

    @Test
    fun archived_view_shows_only_archived() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(NotesListIntent.ToggleArchivedView)
        testScheduler.advanceUntilIdle()
        assertTrue(store.state.value.showArchived)
        assertEquals(listOf(3L), store.state.value.notes.map { it.id })
    }

    @Test
    fun toggle_pin_updates_repository_and_ordering() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(NotesListIntent.TogglePin(1, pinned = true))
        testScheduler.advanceUntilIdle()
        // Both pinned now: ordered by recency among pinned (Beta 300, Alpha 100).
        assertEquals(listOf(2L, 1L), store.state.value.notes.map { it.id })
        assertTrue(repository.notes.value.first { it.id == 1L }.pinned)
    }

    @Test
    fun toggle_layout_switches_between_list_and_grid() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        assertEquals(NotesLayout.List, store.state.value.layout)
        store.onIntent(NotesListIntent.ToggleLayout)
        assertEquals(NotesLayout.Grid, store.state.value.layout)
    }

    @Test
    fun create_note_emits_open_editor_for_new_text_note() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.effects.test {
            store.onIntent(NotesListIntent.CreateNote)
            val effect = awaitItem()
            assertTrue(effect is NotesListEffect.OpenEditor)
            assertEquals(0L, (effect as NotesListEffect.OpenEditor).noteId)
            assertEquals(false, effect.checklist)
        }
    }

    @Test
    fun create_checklist_emits_open_editor_in_checklist_mode() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.effects.test {
            store.onIntent(NotesListIntent.CreateChecklist)
            val effect = awaitItem() as NotesListEffect.OpenEditor
            assertEquals(0L, effect.noteId)
            assertTrue(effect.checklist)
        }
    }
}
