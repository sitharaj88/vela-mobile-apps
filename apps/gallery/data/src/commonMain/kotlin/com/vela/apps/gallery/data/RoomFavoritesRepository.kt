/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.data

import com.vela.apps.gallery.data.local.FavoriteDao
import com.vela.apps.gallery.domain.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-backed [FavoritesRepository]. */
class RoomFavoritesRepository(
    private val dao: FavoriteDao,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<Set<String>> =
        dao.observeIds().map { it.toSet() }

    override suspend fun toggle(id: String) {
        if (dao.isFavorite(id)) dao.remove(id) else dao.add(id)
    }
}
