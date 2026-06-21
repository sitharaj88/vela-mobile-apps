/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.applauncher.domain.AppEntry
import com.vela.apps.applauncher.presentation.AppListEffect
import com.vela.apps.applauncher.presentation.AppListIntent
import com.vela.apps.applauncher.presentation.AppListState
import com.vela.apps.applauncher.presentation.AppListStore
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import org.koin.compose.viewmodel.koinViewModel

/** Root composable for the App Launcher ("Vela Apps") app. */
@Composable
fun AppLauncherApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Slate, themeMode = themeMode, dynamicColor = true) {
        val store: AppListStore = koinViewModel()
        val state by store.state.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedLaunchSnackbar(store, snackbarHostState)

        VelaScaffold(title = "Apps", snackbarHostState = snackbarHostState) { padding ->
            AppLauncherContent(
                state = state,
                padding = padding,
                onSearch = { store.onIntent(AppListIntent.Search(it)) },
                onLaunch = { store.onIntent(AppListIntent.Launch(it)) },
                onToggleFavorite = { store.onIntent(AppListIntent.ToggleFavorite(it)) },
            )
        }
    }
}

@Composable
private fun LaunchedLaunchSnackbar(store: AppListStore, snackbarHostState: SnackbarHostState) {
    LaunchedEffect(store) {
        store.effects.collect { effect ->
            when (effect) {
                is AppListEffect.Launched -> snackbarHostState.showSnackbar("Opening ${effect.label}")
            }
        }
    }
}

@Composable
private fun AppLauncherContent(
    state: AppListState,
    padding: PaddingValues,
    onSearch: (String) -> Unit,
    onLaunch: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val tokens = LocalVelaTokens.current
    Column(Modifier.fillMaxSize().padding(padding)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onSearch,
            singleLine = true,
            enabled = state.supported,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text("Search apps") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.sm),
        )
        when {
            !state.supported -> UnsupportedState()
            state.filtered.isEmpty() && !state.loading -> NoResultsState(state)
            else -> AppGrid(
                state = state,
                onLaunch = onLaunch,
                onToggleFavorite = onToggleFavorite,
            )
        }
    }
}

@Composable
private fun UnsupportedState() {
    VelaEmptyState(
        icon = Icons.Filled.PhoneIphone,
        title = "Not available here",
        description = "Listing and launching installed apps isn't possible on this platform.",
    )
}

@Composable
private fun NoResultsState(state: AppListState) {
    VelaEmptyState(
        icon = Icons.Filled.Apps,
        title = if (state.all.isEmpty()) "No apps found" else "No matches",
        description = when {
            state.all.isEmpty() -> "No launchable applications were found on this device."
            else -> "No apps match \"${state.query}\"."
        },
    )
}

@Composable
private fun AppGrid(
    state: AppListState,
    onLaunch: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val tokens = LocalVelaTokens.current
    val showFavorites = state.query.isBlank() && state.favorites.isNotEmpty()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        contentPadding = PaddingValues(tokens.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.lg),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (showFavorites) {
            sectionHeader("Favorites")
            items(state.favorites, key = { "fav:${it.id}" }) { app ->
                AppTile(app, favorite = true, onLaunch = onLaunch, onToggleFavorite = onToggleFavorite)
            }
            sectionHeader("All apps")
        }
        items(state.filtered, key = { it.id }) { app ->
            AppTile(
                app = app,
                favorite = app.id in state.favoriteIds,
                onLaunch = onLaunch,
                onToggleFavorite = onToggleFavorite,
            )
        }
    }
}

private fun LazyGridScope.sectionHeader(title: String) {
    item(span = { GridItemSpan(maxLineSpan) }, key = "header:$title") {
        SectionTitle(title)
    }
}

@Composable
private fun SectionTitle(title: String) {
    val tokens = LocalVelaTokens.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = tokens.spacing.sm, bottom = tokens.spacing.xs),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: AppEntry,
    favorite: Boolean,
    onLaunch: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val tokens = LocalVelaTokens.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = { onLaunch(app.id) },
                onLongClick = { onToggleFavorite(app.id) },
            )
            .padding(vertical = tokens.spacing.sm),
    ) {
        Monogram(label = app.label, favorite = favorite)
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = tokens.spacing.sm),
        )
    }
}

/** Circular badge with the first letter of the label; a star overlay marks favorites. */
@Composable
private fun Monogram(label: String, favorite: Boolean) {
    val letter = label.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(contentAlignment = Alignment.Center) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        if (favorite) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}
