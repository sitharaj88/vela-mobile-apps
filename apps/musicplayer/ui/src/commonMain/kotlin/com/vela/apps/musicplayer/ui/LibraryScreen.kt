/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.vela.apps.musicplayer.domain.Album
import com.vela.apps.musicplayer.domain.Artist
import com.vela.apps.musicplayer.domain.Track
import com.vela.apps.musicplayer.presentation.LibraryState
import com.vela.apps.musicplayer.presentation.LibraryTab
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.theme.LocalVelaTokens

/** Library with Songs / Albums / Artists tabs. Tapping a song plays it; albums/artists drill down. */
@Composable
fun LibraryScreen(
    state: LibraryState,
    onSelectTab: (LibraryTab) -> Unit,
    currentTrackId: String?,
    onPlay: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = state.tab.ordinal) {
            LibraryTab.entries.forEach { tab ->
                Tab(
                    selected = state.tab == tab,
                    onClick = { onSelectTab(tab) },
                    text = { Text(tab.label()) },
                )
            }
        }
        when {
            state.isLoading -> Centered { CircularProgressIndicator() }
            else -> when (state.tab) {
                LibraryTab.Songs -> SongsTab(state.tracks, currentTrackId, onPlay)
                LibraryTab.Albums -> AlbumsTab(state.albums, onPlay)
                LibraryTab.Artists -> ArtistsTab(state.artists, onPlay)
            }
        }
    }
}

@Composable
private fun SongsTab(tracks: List<Track>, currentTrackId: String?, onPlay: (Track) -> Unit) {
    if (tracks.isEmpty()) {
        EmptyLibrary()
        return
    }
    val tokens = LocalVelaTokens.current
    LazyColumn(Modifier.fillMaxSize()) {
        items(tracks, key = { it.id }) { track ->
            TrackRow(
                track = track,
                isCurrent = track.id == currentTrackId,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlay(track) }
                    .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.md),
            )
        }
    }
}

@Composable
private fun AlbumsTab(albums: List<Album>, onPlay: (Track) -> Unit) {
    if (albums.isEmpty()) {
        EmptyLibrary()
        return
    }
    val tokens = LocalVelaTokens.current
    LazyColumn(Modifier.fillMaxSize()) {
        items(albums, key = { it.name }) { album ->
            GroupRow(
                icon = Icons.Filled.Album,
                title = album.name,
                subtitle = "${album.artist} • ${album.trackCount} ${tracksWord(album.trackCount)}",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { album.tracks.firstOrNull()?.let(onPlay) }
                    .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.md),
            )
        }
    }
}

@Composable
private fun ArtistsTab(artists: List<Artist>, onPlay: (Track) -> Unit) {
    if (artists.isEmpty()) {
        EmptyLibrary()
        return
    }
    val tokens = LocalVelaTokens.current
    LazyColumn(Modifier.fillMaxSize()) {
        items(artists, key = { it.name }) { artist ->
            GroupRow(
                icon = Icons.Filled.Person,
                title = artist.name,
                subtitle = "${artist.albumCount} ${albumsWord(artist.albumCount)} • " +
                    "${artist.trackCount} ${tracksWord(artist.trackCount)}",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { artist.tracks.firstOrNull()?.let(onPlay) }
                    .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.md),
            )
        }
    }
}

@Composable
private fun TrackRow(track: Track, isCurrent: Boolean, modifier: Modifier = Modifier) {
    val tokens = LocalVelaTokens.current
    val titleColor =
        if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = titleColor)
        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (track.durationMs > 0) {
            Text(
                text = formatDuration(track.durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GroupRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalVelaTokens.current
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Centered {
        VelaEmptyState(
            icon = Icons.Filled.MusicNote,
            title = "No tracks found",
            description = "Add audio to your Music library and reopen the app.",
        )
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) { content() }
}

private fun LibraryTab.label(): String = when (this) {
    LibraryTab.Songs -> "Songs"
    LibraryTab.Albums -> "Albums"
    LibraryTab.Artists -> "Artists"
}

private fun tracksWord(count: Int): String = if (count == 1) "track" else "tracks"
private fun albumsWord(count: Int): String = if (count == 1) "album" else "albums"
