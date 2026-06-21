/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A locally-stored "starred" marker for a read-only system contact (Android/iOS). The system store
 * is not writable in v1, so favorites live here keyed by the platform contact id.
 */
@Entity(tableName = "contact_favorites")
data class FavoriteEntity(
    @PrimaryKey val contactId: String,
)
