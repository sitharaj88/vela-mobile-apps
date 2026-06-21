/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.presentation.editor

import androidx.lifecycle.viewModelScope
import com.vela.apps.notes.domain.model.ChecklistItem
import com.vela.apps.notes.domain.model.Note
import com.vela.apps.notes.domain.model.NoteType
import com.vela.apps.notes.domain.model.TagCodec
import com.vela.apps.notes.domain.usecase.DeleteNoteUseCase
import com.vela.apps.notes.domain.usecase.ObserveNoteUseCase
import com.vela.apps.notes.domain.usecase.SaveNoteUseCase
import com.vela.core.common.MviStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Edits a single note. A [noteId] of 0 starts a new note (in [startAsChecklist] mode if requested).
 * The note is auto-saved on [Close] (and discarded if left blank), mirroring familiar notes-app
 * behavior. New checklist items get monotonically-decreasing negative ids so they never collide
 * with persisted ids until Room assigns real ones on save.
 */
class NoteEditorStore(
    private val noteId: Long,
    startAsChecklist: Boolean,
    private val observeNote: ObserveNoteUseCase,
    private val saveNote: SaveNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
) : MviStore<NoteEditorState, NoteEditorIntent, NoteEditorEffect>(
    NoteEditorState(
        id = noteId,
        type = if (startAsChecklist) NoteType.CHECKLIST else NoteType.TEXT,
        items = if (startAsChecklist) listOf(ChecklistItem(id = FIRST_TEMP_ID, text = "")) else emptyList(),
    ),
) {

    private var nextTempId = FIRST_TEMP_ID - 1

    init {
        if (noteId != 0L) {
            viewModelScope.launch {
                observeNote(noteId).first()?.let { note ->
                    setState {
                        copy(
                            title = note.title,
                            content = note.content,
                            type = note.type,
                            items = note.items,
                            pinned = note.pinned,
                            archived = note.archived,
                            color = note.color,
                            tags = note.tags,
                            createdAt = note.createdAt,
                        )
                    }
                }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override fun onIntent(intent: NoteEditorIntent) {
        when (intent) {
            is NoteEditorIntent.TitleChanged -> setState { copy(title = intent.title) }
            is NoteEditorIntent.ContentChanged -> setState { copy(content = intent.content) }
            NoteEditorIntent.ToggleType -> toggleType()

            is NoteEditorIntent.ItemTextChanged ->
                setState { copy(items = items.mapItem(intent.id) { it.copy(text = intent.text) }) }
            is NoteEditorIntent.ItemCheckedChanged ->
                setState { copy(items = items.mapItem(intent.id) { it.copy(checked = intent.checked) }) }
            NoteEditorIntent.AddItem ->
                setState { copy(items = items + ChecklistItem(id = nextTempId--, text = "")) }
            is NoteEditorIntent.RemoveItem ->
                setState { copy(items = items.filterNot { it.id == intent.id }) }
            is NoteEditorIntent.MoveItem -> setState { copy(items = items.moved(intent.from, intent.to)) }

            is NoteEditorIntent.ColorChanged -> setState { copy(color = intent.color) }
            NoteEditorIntent.TogglePin -> setState { copy(pinned = !pinned) }
            NoteEditorIntent.ToggleArchive -> setState { copy(archived = !archived) }
            is NoteEditorIntent.TagsChanged -> setState { copy(tags = TagCodec.decode(intent.raw)) }

            NoteEditorIntent.Delete -> viewModelScope.launch {
                if (!currentState.isNew) deleteNote(currentState.id)
                emitEffect(NoteEditorEffect.NavigateBack)
            }
            NoteEditorIntent.Close -> viewModelScope.launch {
                saveNote(currentState.toNote())
                emitEffect(NoteEditorEffect.NavigateBack)
            }
        }
    }

    private fun toggleType() = setState {
        val newType = if (type == NoteType.TEXT) NoteType.CHECKLIST else NoteType.TEXT
        val seededItems = if (newType == NoteType.CHECKLIST && items.isEmpty()) {
            listOf(ChecklistItem(id = nextTempId--, text = ""))
        } else {
            items
        }
        copy(type = newType, items = seededItems)
    }

    private fun NoteEditorState.toNote(): Note {
        // Drop blank checklist items so empties typed mid-edit don't persist.
        val cleanItems = items.filter { it.text.isNotBlank() }
        return Note(
            id = id,
            title = title,
            content = if (type == NoteType.TEXT) content else "",
            type = type,
            items = if (type == NoteType.CHECKLIST) cleanItems else emptyList(),
            pinned = pinned,
            archived = archived,
            color = color,
            tags = tags,
            createdAt = createdAt ?: Clock.System.now(),
            updatedAt = Clock.System.now(),
        )
    }

    private companion object {
        const val FIRST_TEMP_ID = -1L
    }
}

private fun List<ChecklistItem>.mapItem(id: Long, transform: (ChecklistItem) -> ChecklistItem) =
    map { if (it.id == id) transform(it) else it }

private fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices || from == to) return this
    val mutable = toMutableList()
    mutable.add(to, mutable.removeAt(from))
    return mutable
}
