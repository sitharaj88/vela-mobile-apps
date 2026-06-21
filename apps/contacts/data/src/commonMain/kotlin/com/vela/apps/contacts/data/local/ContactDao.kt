/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** CRUD for the local Desktop address book. */
@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY displayName COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    fun observeById(id: Long): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun findById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts ORDER BY displayName COLLATE NOCASE ASC")
    suspend fun list(): List<ContactEntity>

    @Upsert
    suspend fun upsert(contact: ContactEntity): Long

    @Query("UPDATE contacts SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
