/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.presentation

import androidx.lifecycle.viewModelScope
import com.vela.apps.musicplayer.domain.Album
import com.vela.apps.musicplayer.domain.Artist
import com.vela.apps.musicplayer.domain.MusicLibrary
import com.vela.apps.musicplayer.domain.Track
import com.vela.apps.musicplayer.domain.groupIntoAlbums
import com.vela.apps.musicplayer.domain.groupIntoArtists
import com.vela.apps.musicplayer.domain.sortedByTitle
import com.vela.core.common.MviStore
import kotlinx.coroutines.launch

/** Which collection the library screen is showing. */
enum class LibraryTab { Songs, Albums, Artists }

data class LibraryState(
    val isLoading: Boolean = true,
    val tab: LibraryTab = LibraryTab.Songs,
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
)

sealed interface LibraryIntent {
    data object Reload : LibraryIntent
    data class SelectTab(val tab: LibraryTab) : LibraryIntent
}

/** Loads the track list from [MusicLibrary] on creation and on explicit reload; derives groupings. */
class LibraryStore(private val library: MusicLibrary) :
    MviStore<LibraryState, LibraryIntent, Nothing>(LibraryState()) {

    init {
        load()
    }

    override fun onIntent(intent: LibraryIntent) {
        when (intent) {
            LibraryIntent.Reload -> load()
            is LibraryIntent.SelectTab -> setState { copy(tab = intent.tab) }
        }
    }

    private fun load() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            val tracks = library.queryTracks().sortedByTitle()
            setState {
                copy(
                    isLoading = false,
                    tracks = tracks,
                    albums = tracks.groupIntoAlbums(),
                    artists = tracks.groupIntoArtists(),
                )
            }
        }
    }
}
