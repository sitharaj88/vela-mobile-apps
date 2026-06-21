/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.data

import com.vela.apps.notes.data.local.NoteDao
import com.vela.apps.notes.data.local.NoteEntity
import com.vela.apps.notes.domain.model.ChecklistCodec
import com.vela.apps.notes.domain.model.Note
import com.vela.apps.notes.domain.model.NoteColor
import com.vela.apps.notes.domain.model.NoteType
import com.vela.apps.notes.domain.model.TagCodec
import com.vela.apps.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/** Room-backed [NoteRepository]; maps between the Room entity and the domain model. */
class RoomNoteRepository(
    private val dao: NoteDao,
) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> =
        dao.observe().map { rows -> rows.map(NoteEntity::toDomain) }

    override fun observeNote(id: Long): Flow<Note?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun save(note: Note): Long =
        dao.upsert(note.toEntity())

    override suspend fun setPinned(id: Long, pinned: Boolean) = dao.setPinned(id, pinned)

    override suspend fun setArchived(id: Long, archived: Boolean) = dao.setArchived(id, archived)

    override suspend fun delete(id: Long) = dao.deleteById(id)
}

private fun NoteEntity.toDomain() = Note(
    id = id,
    title = title,
    content = content,
    type = runCatching { NoteType.valueOf(type) }.getOrDefault(NoteType.TEXT),
    items = ChecklistCodec.decode(items),
    pinned = pinned,
    archived = archived,
    color = runCatching { NoteColor.valueOf(color) }.getOrDefault(NoteColor.None),
    tags = TagCodec.decode(tags),
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs),
    updatedAt = Instant.fromEpochMilliseconds(updatedAtEpochMs),
)

private fun Note.toEntity() = NoteEntity(
    id = id,
    title = title,
    content = content,
    type = type.name,
    items = ChecklistCodec.encode(items),
    pinned = pinned,
    archived = archived,
    color = color.name,
    tags = TagCodec.encode(tags),
    createdAtEpochMs = createdAt.toEpochMilliseconds(),
    updatedAtEpochMs = updatedAt.toEpochMilliseconds(),
)
