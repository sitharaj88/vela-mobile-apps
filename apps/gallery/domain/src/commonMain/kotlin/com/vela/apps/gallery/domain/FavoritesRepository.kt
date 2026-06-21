/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.domain

import kotlinx.coroutines.flow.Flow

/** Persists the set of favorited [MediaItem.id]s. Backed by Room on every platform. */
interface FavoritesRepository {

    /** Observes the current set of favorited media ids. */
    fun observeFavorites(): Flow<Set<String>>

    /** Adds [id] to favorites if absent, otherwise removes it. */
    suspend fun toggle(id: String)
}
