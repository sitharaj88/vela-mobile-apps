/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.domain

import kotlinx.coroutines.flow.Flow

/** Persists the set of app ids the user has starred as favorites. */
interface FavoritesRepository {

    /** Emits the current set of favorite app ids, updating as the user stars/unstars apps. */
    fun observeFavorites(): Flow<Set<String>>

    /** Adds [id] to favorites if absent, otherwise removes it. */
    suspend fun toggle(id: String)
}
