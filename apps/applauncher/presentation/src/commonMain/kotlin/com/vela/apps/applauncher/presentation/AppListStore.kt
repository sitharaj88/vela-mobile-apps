/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.presentation

import androidx.lifecycle.viewModelScope
import com.vela.apps.applauncher.domain.AppEntry
import com.vela.apps.applauncher.domain.AppListLogic
import com.vela.apps.applauncher.domain.FavoritesRepository
import com.vela.apps.applauncher.domain.InstalledApps
import com.vela.core.common.MviStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Immutable launcher state.
 *
 * @param query current search text.
 * @param all every installed app, sorted alphabetically.
 * @param filtered [all] narrowed by [query] (what the grid renders).
 * @param favorites the starred apps (sorted), shown in a pinned section when the query is blank.
 * @param favoriteIds fast-lookup set powering the star toggle on each tile.
 * @param loading true only during the initial enumeration.
 * @param supported false on platforms that cannot enumerate apps (iOS) — drives the unsupported UI.
 */
data class AppListState(
    val query: String = "",
    val all: List<AppEntry> = emptyList(),
    val filtered: List<AppEntry> = emptyList(),
    val favorites: List<AppEntry> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val loading: Boolean = true,
    val supported: Boolean = true,
)

sealed interface AppListIntent {
    /** Update the search query and re-filter the list. */
    data class Search(val query: String) : AppListIntent
    /** Launch the app identified by [id]. */
    data class Launch(val id: String) : AppListIntent
    /** Star/unstar the app identified by [id]. */
    data class ToggleFavorite(val id: String) : AppListIntent
}

/** One-shot events surfaced to the UI (e.g. confirming a launch via snackbar). */
sealed interface AppListEffect {
    data class Launched(val label: String) : AppListEffect
}

/**
 * Loads installed apps once, keeps favorites in sync with the repository, and filters by label as
 * the user types. Launching is delegated to the platform [InstalledApps] and confirmed via effect.
 */
class AppListStore(
    private val installedApps: InstalledApps,
    private val favoritesRepository: FavoritesRepository,
) : MviStore<AppListState, AppListIntent, AppListEffect>(
    AppListState(supported = installedApps.isSupported),
) {

    init {
        loadApps()
        observeFavorites()
    }

    override fun onIntent(intent: AppListIntent) {
        when (intent) {
            is AppListIntent.Search -> onSearch(intent.query)
            is AppListIntent.Launch -> onLaunch(intent.id)
            is AppListIntent.ToggleFavorite -> onToggleFavorite(intent.id)
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            val apps = AppListLogic.sort(installedApps.list())
            setState {
                copy(
                    all = apps,
                    filtered = AppListLogic.filter(apps, query),
                    favorites = AppListLogic.favorites(apps, favoriteIds),
                    loading = false,
                )
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.observeFavorites().collectLatest { ids ->
                setState {
                    copy(
                        favoriteIds = ids,
                        favorites = AppListLogic.favorites(all, ids),
                    )
                }
            }
        }
    }

    private fun onSearch(query: String) = setState {
        copy(query = query, filtered = AppListLogic.filter(all, query))
    }

    private fun onLaunch(id: String) {
        installedApps.launch(id)
        val label = currentState.all.firstOrNull { it.id == id }?.label ?: return
        emitEffect(AppListEffect.Launched(label))
    }

    private fun onToggleFavorite(id: String) {
        viewModelScope.launch { favoritesRepository.toggle(id) }
    }
}
