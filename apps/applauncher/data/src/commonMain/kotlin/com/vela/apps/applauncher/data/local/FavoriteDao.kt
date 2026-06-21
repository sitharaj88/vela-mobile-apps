/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT appId FROM favorite_apps")
    fun observe(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_apps WHERE appId = :appId)")
    suspend fun isFavorite(appId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(entity: FavoriteEntity)

    @Query("DELETE FROM favorite_apps WHERE appId = :appId")
    suspend fun remove(appId: String)
}
