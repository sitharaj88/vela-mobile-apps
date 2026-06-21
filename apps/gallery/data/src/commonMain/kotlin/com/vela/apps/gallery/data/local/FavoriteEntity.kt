/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One favorited media item, keyed by the stable [com.vela.apps.gallery.domain.MediaItem.id]. */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val mediaId: String,
)
