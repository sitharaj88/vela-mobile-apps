/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as columnItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vela.apps.gallery.domain.ALL_MEDIA_ALBUM
import com.vela.apps.gallery.domain.Album
import com.vela.apps.gallery.domain.FAVORITES_ALBUM
import com.vela.apps.gallery.presentation.AlbumViewMode
import com.vela.apps.gallery.presentation.GalleryState
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens

private const val ALBUM_COLUMNS = 2

/** Top-level screen: a grid (or list) of albums plus synthetic "All media" and "Favorites". */
@Composable
internal fun AlbumsScreen(
    state: GalleryState,
    snackbar: SnackbarHostState,
    onOpenAlbum: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    onRefresh: () -> Unit,
) {
    VelaScaffold(
        title = "Gallery",
        snackbarHostState = snackbar,
        actions = {
            IconButton(onClick = onToggleViewMode) {
                val grid = state.viewMode == AlbumViewMode.Grid
                Icon(
                    imageVector = if (grid) Icons.Filled.ViewList else Icons.Filled.GridView,
                    contentDescription = "Toggle view",
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.allItems.isEmpty() -> VelaEmptyState(
                    icon = Icons.Filled.PhotoLibrary,
                    title = "No media",
                    description = "Photos and videos will appear here.",
                )
                else -> AlbumsContent(state, onOpenAlbum)
            }
        }
    }
}

@Composable
private fun AlbumsContent(state: GalleryState, onOpenAlbum: (String) -> Unit) {
    val favoriteCount = state.allItems.count { it.id in state.favorites }
    val synthetic = buildList {
        add(syntheticAlbum(ALL_MEDIA_ALBUM, state.allItems.size, state.albums.firstOrNull()?.coverUri))
        add(syntheticAlbum(FAVORITES_ALBUM, favoriteCount, null))
    }
    val albums = synthetic + state.albums
    if (state.viewMode == AlbumViewMode.Grid) {
        AlbumsGrid(albums, onOpenAlbum)
    } else {
        AlbumsList(albums, onOpenAlbum)
    }
}

@Composable
private fun AlbumsGrid(albums: List<Album>, onOpenAlbum: (String) -> Unit) {
    val tokens = LocalVelaTokens.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(ALBUM_COLUMNS),
        modifier = Modifier.fillMaxSize().padding(tokens.spacing.sm),
    ) {
        items(albums, key = { it.name }) { album ->
            AlbumGridCard(album) { onOpenAlbum(album.name) }
        }
    }
}

@Composable
private fun AlbumGridCard(album: Album, onClick: () -> Unit) {
    val tokens = LocalVelaTokens.current
    Column(
        modifier = Modifier
            .padding(tokens.spacing.xs)
            .clip(RoundedCornerShape(tokens.spacing.md))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(tokens.spacing.md))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AlbumCover(album, Modifier.fillMaxSize())
        }
        Text(
            text = album.name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = tokens.spacing.xs),
        )
        Text(
            text = "${album.itemCount} items",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AlbumsList(albums: List<Album>, onOpenAlbum: (String) -> Unit) {
    val tokens = LocalVelaTokens.current
    LazyColumn(modifier = Modifier.fillMaxSize().padding(tokens.spacing.sm)) {
        columnItems(albums, key = { it.name }) { album ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = tokens.spacing.xs)
                    .clip(RoundedCornerShape(tokens.spacing.md))
                    .clickable { onOpenAlbum(album.name) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(tokens.spacing.sm))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    AlbumCover(album, Modifier.fillMaxSize())
                }
                Column(Modifier.padding(start = tokens.spacing.md)) {
                    Text(album.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${album.itemCount} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumCover(album: Album, modifier: Modifier) {
    val cover = album.coverUri
    if (album.name == FAVORITES_ALBUM || cover == null) {
        Icon(
            imageVector = if (album.name == FAVORITES_ALBUM) Icons.Filled.Favorite else Icons.Filled.PhotoLibrary,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(12.dp),
        )
    } else {
        AsyncImage(
            model = cover,
            contentDescription = album.name,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

private fun syntheticAlbum(name: String, count: Int, cover: String?) =
    Album(name = name, itemCount = count, coverUri = cover, newestDateMs = Long.MAX_VALUE)
