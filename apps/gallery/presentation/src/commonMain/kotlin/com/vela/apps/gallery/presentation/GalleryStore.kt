/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.presentation

import androidx.lifecycle.viewModelScope
import com.vela.apps.gallery.domain.FAVORITES_ALBUM
import com.vela.apps.gallery.domain.FavoritesRepository
import com.vela.apps.gallery.domain.GalleryGrouping
import com.vela.apps.gallery.domain.MediaSource
import com.vela.core.common.MviStore
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Loads media from the injected [mediaSource], observes [favoritesRepository], and derives the
 * albums grid + per-album photo list applying the current sort and search query. All grouping,
 * sorting and filtering is delegated to the pure [GalleryGrouping] helpers.
 */
class GalleryStore(
    private val mediaSource: MediaSource,
    private val favoritesRepository: FavoritesRepository,
) : MviStore<GalleryState, GalleryIntent, GalleryEffect>(GalleryState()) {

    init {
        observeFavorites()
        load()
    }

    override fun onIntent(intent: GalleryIntent) {
        when (intent) {
            GalleryIntent.Refresh -> load()
            is GalleryIntent.OpenAlbum -> setState { recompute(copy(selectedAlbum = intent.album)) }
            is GalleryIntent.SetSort -> setState { recompute(copy(sort = intent.sort)) }
            is GalleryIntent.SetQuery -> setState { recompute(copy(query = intent.query)) }
            GalleryIntent.ToggleViewMode -> setState { copy(viewMode = nextViewMode(viewMode)) }
            is GalleryIntent.ToggleFavorite -> toggleFavorite(intent.id)
        }
    }

    private fun observeFavorites() {
        favoritesRepository.observeFavorites()
            .onEach { favorites -> setState { recompute(copy(favorites = favorites)) } }
            .launchIn(viewModelScope)
    }

    private fun toggleFavorite(id: String) {
        viewModelScope.launch { favoritesRepository.toggle(id) }
    }

    @Suppress("TooGenericExceptionCaught") // Platform media queries can fail in varied ways.
    private fun load() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val items = mediaSource.allItems()
                setState { recompute(copy(allItems = items, isLoading = false)) }
            } catch (error: Exception) {
                val message = error.message ?: "Could not load media."
                setState { copy(isLoading = false, error = message) }
                emitEffect(GalleryEffect.ShowError(message))
            }
        }
    }

    private companion object {
        fun nextViewMode(mode: AlbumViewMode): AlbumViewMode =
            if (mode == AlbumViewMode.Grid) AlbumViewMode.List else AlbumViewMode.Grid

        /** Recomputes [GalleryState.albums] and [GalleryState.visibleItems] from inputs. */
        fun recompute(state: GalleryState): GalleryState {
            val albums = GalleryGrouping.albums(state.allItems)
            val selected = state.selectedAlbum
            val base = when (selected) {
                null -> emptyList()
                FAVORITES_ALBUM -> state.allItems.filter { it.id in state.favorites }
                else -> GalleryGrouping.itemsInAlbum(state.allItems, selected)
            }
            val filtered = GalleryGrouping.filter(base, state.query)
            val sorted = GalleryGrouping.sort(filtered, state.sort)
            return state.copy(albums = albums, visibleItems = sorted)
        }
    }
}
