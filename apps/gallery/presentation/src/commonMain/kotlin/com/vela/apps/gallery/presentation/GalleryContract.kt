/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.presentation

import com.vela.apps.gallery.domain.Album
import com.vela.apps.gallery.domain.MediaItem
import com.vela.apps.gallery.domain.MediaSort

/** Albums can be presented as a grid of covers or a compact list. */
enum class AlbumViewMode { Grid, List }

/**
 * Full UI state for the Gallery.
 *
 * @property allItems every loaded media item (unfiltered, unsorted) — the working set.
 * @property albums derived album summaries for the albums screen.
 * @property selectedAlbum the album whose photos are shown, or `null` on the albums screen.
 * @property visibleItems items for the selected album after sort + query are applied.
 * @property favorites set of favorited media ids.
 * @property sort current ordering.
 * @property query current search text.
 * @property viewMode albums grid/list toggle.
 * @property isLoading initial load flag.
 * @property error human-readable load error, or `null`.
 */
data class GalleryState(
    val allItems: List<MediaItem> = emptyList(),
    val albums: List<Album> = emptyList(),
    val selectedAlbum: String? = null,
    val visibleItems: List<MediaItem> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val sort: MediaSort = MediaSort.DateDesc,
    val query: String = "",
    val viewMode: AlbumViewMode = AlbumViewMode.Grid,
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface GalleryIntent {
    /** Re-query the platform media source. */
    data object Refresh : GalleryIntent

    /** Open an album (or `null` to return to the albums screen). */
    data class OpenAlbum(val album: String?) : GalleryIntent

    /** Change the ordering of the photo grid. */
    data class SetSort(val sort: MediaSort) : GalleryIntent

    /** Update the search query. */
    data class SetQuery(val query: String) : GalleryIntent

    /** Toggle the albums grid/list presentation. */
    data object ToggleViewMode : GalleryIntent

    /** Add/remove a media item from favorites. */
    data class ToggleFavorite(val id: String) : GalleryIntent
}

/** One-shot events surfaced to the UI (e.g. a load failure snackbar). */
sealed interface GalleryEffect {
    data class ShowError(val message: String) : GalleryEffect
}
