/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row for a note.
 *
 * Structured data (checklist [items] and [tags]) is stored as encoded strings (see
 * `ChecklistCodec`/`TagCodec`) so the schema stays a single flat table. [type] and [color] are
 * persisted as their enum names. Defaults on the new (v2) columns make the migration trivial.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val updatedAtEpochMs: Long,
    @ColumnInfo(defaultValue = "0") val createdAtEpochMs: Long = 0,
    @ColumnInfo(defaultValue = "TEXT") val type: String = "TEXT",
    @ColumnInfo(defaultValue = "") val items: String = "",
    @ColumnInfo(defaultValue = "0") val pinned: Boolean = false,
    @ColumnInfo(defaultValue = "0") val archived: Boolean = false,
    @ColumnInfo(defaultValue = "None") val color: String = "None",
    @ColumnInfo(defaultValue = "") val tags: String = "",
)
