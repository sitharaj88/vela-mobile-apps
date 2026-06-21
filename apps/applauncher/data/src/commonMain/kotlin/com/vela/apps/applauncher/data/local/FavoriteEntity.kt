/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One starred app, keyed by its platform launch id (package name / shortcut path). */
@Entity(tableName = "favorite_apps")
data class FavoriteEntity(
    @PrimaryKey val appId: String,
)
