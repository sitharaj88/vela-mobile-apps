/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.ui.di

import com.vela.apps.notes.data.di.notesDataModule
import com.vela.apps.notes.presentation.di.notesPresentationModule
import com.vela.apps.notes.presentation.editor.NoteEditorStore
import com.vela.apps.notes.presentation.list.NotesListStore
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole Notes app. */
val notesModule = module {
    includes(notesDataModule, notesPresentationModule)
    viewModelOf(::NotesListStore)
    // The editor receives its note id (0 = new) and whether to start as a checklist as parameters.
    viewModel { (noteId: Long, checklist: Boolean) ->
        NoteEditorStore(noteId, checklist, get(), get(), get())
    }
}
