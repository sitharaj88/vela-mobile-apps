/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vela.apps.gallery.domain.MediaItem
import com.vela.apps.gallery.domain.MediaSort
import com.vela.apps.gallery.presentation.GalleryState
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens

private const val GRID_COLUMNS = 3

/** Photos of the selected album in a square grid, with sort + search and a back action. */
@Composable
internal fun PhotoGridScreen(
    state: GalleryState,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onSetSort: (MediaSort) -> Unit,
    onSetQuery: (String) -> Unit,
    onOpenItem: (Int) -> Unit,
) {
    var sortMenu by remember { mutableStateOf(false) }
    var searching by remember { mutableStateOf(false) }
    VelaScaffold(
        title = state.selectedAlbum.orEmpty(),
        snackbarHostState = snackbar,
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = onBack,
        actions = {
            IconButton(onClick = { searching = !searching }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
            Box {
                IconButton(onClick = { sortMenu = true }) {
                    Icon(Icons.Filled.Sort, contentDescription = "Sort")
                }
                SortMenu(expanded = sortMenu, current = state.sort, onDismiss = { sortMenu = false }) {
                    onSetSort(it)
                    sortMenu = false
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (searching) {
                val tokens = LocalVelaTokens.current
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onSetQuery,
                    singleLine = true,
                    placeholder = { Text("Search by name") },
                    modifier = Modifier.fillMaxWidth().padding(tokens.spacing.md),
                )
            }
            PhotoGrid(state.visibleItems, state.favorites, onOpenItem)
        }
    }
}

@Composable
private fun SortMenu(
    expanded: Boolean,
    current: MediaSort,
    onDismiss: () -> Unit,
    onSelect: (MediaSort) -> Unit,
) {
    val labels = listOf(
        MediaSort.DateDesc to "Newest first",
        MediaSort.DateAsc to "Oldest first",
        MediaSort.NameAsc to "Name A-Z",
        MediaSort.NameDesc to "Name Z-A",
    )
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        labels.forEach { (sort, label) ->
            DropdownMenuItem(
                text = { Text(if (sort == current) "$label  ✓" else label) },
                onClick = { onSelect(sort) },
            )
        }
    }
}

@Composable
private fun PhotoGrid(items: List<MediaItem>, favorites: Set<String>, onOpen: (Int) -> Unit) {
    val tokens = LocalVelaTokens.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        modifier = Modifier.fillMaxSize().padding(tokens.spacing.xs),
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            MediaThumbnail(item, item.id in favorites) { onOpen(index) }
        }
    }
}

@Composable
private fun MediaThumbnail(item: MediaItem, isFavorite: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (item.isVideo) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center).size(36.dp),
            )
        }
        if (isFavorite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorite",
                tint = Color.White,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(18.dp),
            )
        }
    }
}
