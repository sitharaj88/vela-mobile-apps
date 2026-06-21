/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Local favorites overlay for read-only system contacts (Android/iOS). */
@Dao
interface FavoriteDao {

    @Query("SELECT contactId FROM contact_favorites")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT contactId FROM contact_favorites")
    suspend fun ids(): List<String>

    @Upsert
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM contact_favorites WHERE contactId = :id")
    suspend fun remove(id: String)
}
