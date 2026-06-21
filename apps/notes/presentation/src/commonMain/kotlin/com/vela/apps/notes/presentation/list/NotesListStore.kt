/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.presentation.list

import androidx.lifecycle.viewModelScope
import com.vela.apps.notes.domain.model.NoteListQuery
import com.vela.apps.notes.domain.model.collectTags
import com.vela.apps.notes.domain.usecase.ObserveAllNotesUseCase
import com.vela.apps.notes.domain.usecase.ObserveNotesUseCase
import com.vela.apps.notes.domain.usecase.SetArchivedUseCase
import com.vela.apps.notes.domain.usecase.SetPinnedUseCase
import com.vela.core.common.MviStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Holds the searchable/sortable/filterable notes list. The query (search + sort + tag + archive)
 * re-queries the repository reactively; the raw stream also feeds the available tag chips. Pinned
 * notes float to the top (handled in the domain query).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotesListStore(
    observeNotes: ObserveNotesUseCase,
    observeAllNotes: ObserveAllNotesUseCase,
    private val setPinned: SetPinnedUseCase,
    private val setArchived: SetArchivedUseCase,
) : MviStore<NotesListState, NotesListIntent, NotesListEffect>(NotesListState()) {

    private val queryFlow = MutableStateFlow(NoteListQuery())

    init {
        queryFlow
            .flatMapLatest { query -> observeNotes(query) }
            .onEach { notes -> setState { copy(notes = notes, isLoading = false) } }
            .launchIn(viewModelScope)

        observeAllNotes()
            .onEach { all -> setState { copy(tags = collectTags(all)) } }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: NotesListIntent) {
        when (intent) {
            is NotesListIntent.QueryChanged -> {
                setState { copy(query = intent.query) }
                updateQuery { copy(search = intent.query) }
            }
            is NotesListIntent.SortChanged -> {
                setState { copy(sort = intent.sort) }
                updateQuery { copy(sort = intent.sort) }
            }
            is NotesListIntent.TagSelected -> {
                setState { copy(selectedTag = intent.tag) }
                updateQuery { copy(tag = intent.tag) }
            }
            NotesListIntent.ToggleArchivedView -> {
                val next = !currentState.showArchived
                setState { copy(showArchived = next) }
                updateQuery { copy(showArchived = next) }
            }
            NotesListIntent.ToggleLayout -> setState {
                copy(layout = if (layout == NotesLayout.List) NotesLayout.Grid else NotesLayout.List)
            }
            is NotesListIntent.TogglePin -> viewModelScope.launch { setPinned(intent.id, intent.pinned) }
            is NotesListIntent.ToggleArchive ->
                viewModelScope.launch { setArchived(intent.id, intent.archived) }
            is NotesListIntent.OpenNote -> emitEffect(NotesListEffect.OpenEditor(intent.id))
            NotesListIntent.CreateNote -> emitEffect(NotesListEffect.OpenEditor(NEW_NOTE_ID))
            NotesListIntent.CreateChecklist ->
                emitEffect(NotesListEffect.OpenEditor(NEW_NOTE_ID, checklist = true))
        }
    }

    private fun updateQuery(reducer: NoteListQuery.() -> NoteListQuery) {
        queryFlow.value = queryFlow.value.reducer()
    }

    private companion object {
        const val NEW_NOTE_ID = 0L
    }
}
