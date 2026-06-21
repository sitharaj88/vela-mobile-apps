/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.gallery.presentation.GalleryEffect
import com.vela.apps.gallery.presentation.GalleryIntent
import com.vela.apps.gallery.presentation.GalleryStore
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import org.koin.compose.viewmodel.koinViewModel

/** Root composable for the Gallery app: albums -> photo grid -> full-screen viewer. */
@Composable
fun GalleryApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Plum, themeMode = themeMode, dynamicColor = true) {
        val store: GalleryStore = koinViewModel()
        val state by store.state.collectAsStateWithLifecycle()
        val snackbar = remember { SnackbarHostState() }
        var viewerIndex by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(store) {
            store.effects.collect { effect ->
                if (effect is GalleryEffect.ShowError) snackbar.showSnackbar(effect.message)
            }
        }

        if (state.selectedAlbum == null) {
            AlbumsScreen(
                state = state,
                snackbar = snackbar,
                onOpenAlbum = { store.onIntent(GalleryIntent.OpenAlbum(it)) },
                onToggleViewMode = { store.onIntent(GalleryIntent.ToggleViewMode) },
                onRefresh = { store.onIntent(GalleryIntent.Refresh) },
            )
        } else {
            PhotoGridScreen(
                state = state,
                snackbar = snackbar,
                onBack = { store.onIntent(GalleryIntent.OpenAlbum(null)) },
                onSetSort = { store.onIntent(GalleryIntent.SetSort(it)) },
                onSetQuery = { store.onIntent(GalleryIntent.SetQuery(it)) },
                onOpenItem = { viewerIndex = it },
            )
        }

        val openIndex = viewerIndex
        if (openIndex != null && state.visibleItems.isNotEmpty()) {
            MediaViewer(
                items = state.visibleItems,
                initialIndex = openIndex.coerceIn(0, state.visibleItems.lastIndex),
                favorites = state.favorites,
                onToggleFavorite = { store.onIntent(GalleryIntent.ToggleFavorite(it)) },
                onDismiss = { viewerIndex = null },
            )
        }
    }
}
