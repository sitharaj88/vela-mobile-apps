/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data

import com.vela.apps.applauncher.data.local.FavoriteDao
import com.vela.apps.applauncher.data.local.FavoriteEntity
import com.vela.apps.applauncher.domain.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-backed [FavoritesRepository] storing one row per starred app id. */
class RoomFavoritesRepository(
    private val dao: FavoriteDao,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<Set<String>> =
        dao.observe().map { it.toSet() }

    override suspend fun toggle(id: String) {
        if (dao.isFavorite(id)) {
            dao.remove(id)
        } else {
            dao.add(FavoriteEntity(id))
        }
    }
}
