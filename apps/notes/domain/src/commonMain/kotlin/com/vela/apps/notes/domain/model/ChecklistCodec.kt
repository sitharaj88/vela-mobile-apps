/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.domain.model

/**
 * Compact, dependency-free codec for persisting checklist items as a single string column.
 *
 * Each item is one record `id<US>checked<US>text` using the ASCII Unit Separator (0x1F) as the
 * field delimiter and the Record Separator (0x1E) between items. Both control chars are stripped
 * from item text on encode so a round-trip is always well-formed. We avoid kotlinx.serialization
 * here to keep the domain/data modules dependency-light and the format trivially diffable.
 */
object ChecklistCodec {

    private val FIELD: Char = Char(0x1F) // ASCII Unit Separator
    private val RECORD: Char = Char(0x1E) // ASCII Record Separator

    fun encode(items: List<ChecklistItem>): String =
        items.joinToString(RECORD.toString()) { item ->
            val text = item.text.replace(FIELD, ' ').replace(RECORD, ' ')
            "${item.id}$FIELD${if (item.checked) '1' else '0'}$FIELD$text"
        }

    fun decode(raw: String): List<ChecklistItem> {
        if (raw.isEmpty()) return emptyList()
        return raw.split(RECORD).mapNotNull { record ->
            val parts = record.split(FIELD, limit = 3)
            if (parts.size < 3) return@mapNotNull null
            val id = parts[0].toLongOrNull() ?: return@mapNotNull null
            ChecklistItem(id = id, text = parts[2], checked = parts[1] == "1")
        }
    }
}

/**
 * Codec for the simple comma-separated tag column. Tags are trimmed, lower-cased, de-duplicated,
 * and blanks/commas dropped so the stored value is canonical.
 */
object TagCodec {

    fun encode(tags: List<String>): String =
        normalize(tags).joinToString(",")

    fun decode(raw: String): List<String> =
        normalize(raw.split(","))

    fun normalize(tags: List<String>): List<String> =
        tags.map { it.trim().lowercase().replace(",", "") }
            .filter { it.isNotEmpty() }
            .distinct()
}
