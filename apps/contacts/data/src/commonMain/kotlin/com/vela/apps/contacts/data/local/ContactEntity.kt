/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A locally-stored contact (Desktop's address book). Phone numbers and emails are persisted as
 * newline-separated strings to avoid a relation table for this single-user store.
 */
@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val phones: String,
    val emails: String,
    val organization: String?,
    val isFavorite: Boolean,
)
