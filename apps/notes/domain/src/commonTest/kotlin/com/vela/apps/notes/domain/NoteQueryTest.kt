/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.domain

import com.vela.apps.notes.domain.model.ChecklistItem
import com.vela.apps.notes.domain.model.Note
import com.vela.apps.notes.domain.model.NoteListQuery
import com.vela.apps.notes.domain.model.NoteSort
import com.vela.apps.notes.domain.model.NoteType
import com.vela.apps.notes.domain.model.applyNoteQuery
import com.vela.apps.notes.domain.model.collectTags
import com.vela.apps.notes.domain.model.noteMatchesSearch
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoteQueryTest {

    private fun note(
        id: Long,
        title: String = "",
        content: String = "",
        created: Long = 0,
        updated: Long = 0,
        pinned: Boolean = false,
        archived: Boolean = false,
        tags: List<String> = emptyList(),
        type: NoteType = NoteType.TEXT,
        items: List<ChecklistItem> = emptyList(),
    ) = Note(
        id = id,
        title = title,
        content = content,
        type = type,
        items = items,
        pinned = pinned,
        archived = archived,
        tags = tags,
        createdAt = Instant.fromEpochMilliseconds(created),
        updatedAt = Instant.fromEpochMilliseconds(updated),
    )

    @Test
    fun search_matches_title_content_and_items_case_insensitively() {
        val textNote = note(1, title = "Groceries", content = "Buy MILK today")
        assertTrue(noteMatchesSearch(textNote, "milk"))
        assertTrue(noteMatchesSearch(textNote, "GROCER"))
        assertFalse(noteMatchesSearch(textNote, "eggs"))

        val checklist = note(
            2,
            type = NoteType.CHECKLIST,
            items = listOf(ChecklistItem(id = 1, text = "Bread"), ChecklistItem(id = 2, text = "Cheese")),
        )
        assertTrue(noteMatchesSearch(checklist, "cheese"))
        assertFalse(noteMatchesSearch(checklist, "milk"))
    }

    @Test
    fun blank_search_matches_everything() {
        assertTrue(noteMatchesSearch(note(1, title = "x"), ""))
        assertTrue(noteMatchesSearch(note(1, title = "x"), "   "))
    }

    @Test
    fun sort_by_modified_orders_newest_first() {
        val notes = listOf(
            note(1, title = "old", updated = 100),
            note(2, title = "new", updated = 300),
            note(3, title = "mid", updated = 200),
        )
        val sorted = applyNoteQuery(notes, NoteListQuery(sort = NoteSort.Modified))
        assertEquals(listOf(2L, 3L, 1L), sorted.map { it.id })
    }

    @Test
    fun sort_by_created_orders_newest_created_first() {
        val notes = listOf(
            note(1, created = 50, updated = 999),
            note(2, created = 200, updated = 1),
        )
        val sorted = applyNoteQuery(notes, NoteListQuery(sort = NoteSort.Created))
        assertEquals(listOf(2L, 1L), sorted.map { it.id })
    }

    @Test
    fun sort_by_title_is_case_insensitive_ascending() {
        val notes = listOf(
            note(1, title = "banana"),
            note(2, title = "Apple"),
            note(3, title = "cherry"),
        )
        val sorted = applyNoteQuery(notes, NoteListQuery(sort = NoteSort.Title))
        assertEquals(listOf(2L, 1L, 3L), sorted.map { it.id })
    }

    @Test
    fun pinned_notes_float_to_top_regardless_of_sort() {
        val notes = listOf(
            note(1, title = "a", updated = 300),
            note(2, title = "b", updated = 100, pinned = true),
            note(3, title = "c", updated = 200),
        )
        val sorted = applyNoteQuery(notes, NoteListQuery(sort = NoteSort.Modified))
        assertEquals(2L, sorted.first().id)
        assertEquals(listOf(1L, 3L), sorted.drop(1).map { it.id })
    }

    @Test
    fun archive_filter_separates_active_and_archived() {
        val notes = listOf(
            note(1, title = "active", updated = 1),
            note(2, title = "archived", updated = 2, archived = true),
        )
        val active = applyNoteQuery(notes, NoteListQuery(showArchived = false))
        assertEquals(listOf(1L), active.map { it.id })
        val archived = applyNoteQuery(notes, NoteListQuery(showArchived = true))
        assertEquals(listOf(2L), archived.map { it.id })
    }

    @Test
    fun tag_filter_keeps_only_matching_notes() {
        val notes = listOf(
            note(1, title = "work", tags = listOf("work")),
            note(2, title = "home", tags = listOf("home")),
            note(3, title = "both", tags = listOf("work", "home")),
        )
        val workOnly = applyNoteQuery(notes, NoteListQuery(tag = "work"))
        assertEquals(listOf(1L, 3L), workOnly.map { it.id }.sorted())
    }

    @Test
    fun collect_tags_returns_distinct_sorted() {
        val notes = listOf(
            note(1, tags = listOf("Zeta", "alpha")),
            note(2, tags = listOf("alpha", "beta")),
        )
        assertEquals(listOf("alpha", "beta", "Zeta"), collectTags(notes))
    }
}
