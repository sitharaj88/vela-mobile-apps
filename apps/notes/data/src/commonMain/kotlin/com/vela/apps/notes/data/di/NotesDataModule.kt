/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.data.di

import com.vela.apps.notes.data.RoomNoteRepository
import com.vela.apps.notes.data.local.NotesDatabase
import com.vela.apps.notes.data.local.buildNotesDatabase
import com.vela.apps.notes.data.local.notesPlatformDatabaseModule
import com.vela.apps.notes.domain.repository.NoteRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Wires the Notes Room database, DAO, and repository binding. */
val notesDataModule = module {
    includes(notesPlatformDatabaseModule())
    single { buildNotesDatabase(get()) }
    single { get<NotesDatabase>().noteDao() }
    singleOf(::RoomNoteRepository) bind NoteRepository::class
}
