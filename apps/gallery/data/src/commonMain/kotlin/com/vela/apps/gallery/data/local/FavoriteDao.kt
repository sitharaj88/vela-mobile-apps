/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT mediaId FROM favorites")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaId = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Query("INSERT OR IGNORE INTO favorites(mediaId) VALUES (:id)")
    suspend fun add(id: String)

    @Query("DELETE FROM favorites WHERE mediaId = :id")
    suspend fun remove(id: String)
}
