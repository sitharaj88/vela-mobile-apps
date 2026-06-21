/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.filemanager.domain.SortField
import com.vela.apps.filemanager.domain.SortOrder
import com.vela.apps.filemanager.presentation.FileBrowserEffect
import com.vela.apps.filemanager.presentation.FileBrowserIntent
import com.vela.apps.filemanager.presentation.FileBrowserState
import com.vela.apps.filemanager.presentation.FileBrowserStore
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import org.koin.compose.viewmodel.koinViewModel

/** Root composable for the File Manager app. */
@Composable
fun FileManagerApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Sky, themeMode = themeMode, dynamicColor = true) {
        val store: FileBrowserStore = koinViewModel()
        val state by store.state.collectAsStateWithLifecycle()
        FileManagerScreen(state, store::onIntent, store.effectsHost())
    }
}

/** Bridges the store's one-shot effects flow to a Compose snackbar host. */
@Composable
private fun FileBrowserStore.effectsHost(): SnackbarHostState {
    val host = remember { SnackbarHostState() }
    LaunchedEffect(this) {
        effects.collect { effect ->
            val text = when (effect) {
                is FileBrowserEffect.ShowMessage -> effect.text
                is FileBrowserEffect.ShowError -> effect.text
            }
            host.showSnackbar(text)
        }
    }
    return host
}

@Composable
private fun FileManagerScreen(
    state: FileBrowserState,
    onIntent: (FileBrowserIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var showSearch by remember { mutableStateOf(false) }
    var showNewFolder by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<String?>(null) }

    VelaScaffold(
        title = if (state.inSelectionMode) "${state.selection.size} selected" else "Files",
        navigationIcon = if (state.inSelectionMode) Icons.Filled.Close else null,
        onNavigationClick = if (state.inSelectionMode) {
            { onIntent(FileBrowserIntent.ClearSelection) }
        } else {
            null
        },
        snackbarHostState = snackbarHostState,
        actions = {
            if (state.inSelectionMode) {
                SelectionActions(state, onIntent)
            } else {
                BrowseActions(
                    state = state,
                    onIntent = onIntent,
                    showSortMenu = showSortMenu,
                    onSortMenuChange = { showSortMenu = it },
                    onToggleSearch = { showSearch = !showSearch },
                    onNewFolder = { showNewFolder = true },
                )
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (showSearch || state.isSearching) {
                FileSearchBar(
                    query = state.query,
                    onQueryChange = { onIntent(FileBrowserIntent.QueryChanged(it)) },
                    onClose = {
                        showSearch = false
                        onIntent(FileBrowserIntent.ClearSearch)
                    },
                )
            }
            if (!state.isSearching) BreadcrumbBar(state.crumbs) { onIntent(FileBrowserIntent.Open(it)) }
            if (!state.isSearching && state.entries.isNotEmpty()) StorageHeader(state)
            FileListBody(state, onIntent, onRename = { renameTarget = it })
        }
    }

    if (showNewFolder) {
        TextEntryDialog(
            title = "New folder",
            confirmLabel = "Create",
            onConfirm = {
                onIntent(FileBrowserIntent.CreateFolder(it))
                showNewFolder = false
            },
            onDismiss = { showNewFolder = false },
        )
    }
    renameTarget?.let { path ->
        TextEntryDialog(
            title = "Rename",
            confirmLabel = "Rename",
            initial = path.substringAfterLast('/').substringAfterLast('\\'),
            onConfirm = {
                onIntent(FileBrowserIntent.Rename(path, it))
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }
}

@Composable
private fun FileListBody(
    state: FileBrowserState,
    onIntent: (FileBrowserIntent) -> Unit,
    onRename: (String) -> Unit,
) {
    when {
        state.isLoading -> LoadingState()
        state.error != null -> VelaEmptyState(
            icon = Icons.Filled.FolderOpen,
            title = "Cannot open folder",
            description = state.error,
        )
        state.visibleEntries.isEmpty() -> VelaEmptyState(
            icon = Icons.Filled.FolderOpen,
            title = if (state.isSearching) "No matches" else "Empty folder",
            description = if (state.isSearching) "Nothing matched your search." else "There's nothing here.",
        )
        else -> FileList(
            state = state,
            onOpen = { onIntent(FileBrowserIntent.Open(it)) },
            onToggleSelect = { onIntent(FileBrowserIntent.ToggleSelect(it)) },
            onRename = onRename,
            onDelete = { onIntent(FileBrowserIntent.Delete(setOf(it))) },
        )
    }
}

@Composable
private fun LoadingState() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BrowseActions(
    state: FileBrowserState,
    onIntent: (FileBrowserIntent) -> Unit,
    showSortMenu: Boolean,
    onSortMenuChange: (Boolean) -> Unit,
    onToggleSearch: () -> Unit,
    onNewFolder: () -> Unit,
) {
    IconButton(onClick = onToggleSearch) {
        Icon(Icons.Filled.Search, contentDescription = "Search")
    }
    IconButton(onClick = onNewFolder) {
        Icon(Icons.Filled.CreateNewFolder, contentDescription = "New folder")
    }
    IconButton(onClick = { onSortMenuChange(true) }) {
        Icon(Icons.Filled.Sort, contentDescription = "Sort")
    }
    SortMenu(
        expanded = showSortMenu,
        current = state.sortOrder,
        showHidden = state.showHidden,
        onDismiss = { onSortMenuChange(false) },
        onSort = { onIntent(FileBrowserIntent.SortChanged(it)) },
        onToggleHidden = { onIntent(FileBrowserIntent.ToggleHidden) },
    )
}

@Composable
private fun SelectionActions(state: FileBrowserState, onIntent: (FileBrowserIntent) -> Unit) {
    IconButton(onClick = { onIntent(FileBrowserIntent.SelectAll) }) {
        Icon(Icons.Filled.SelectAll, contentDescription = "Select all")
    }
    IconButton(onClick = { onIntent(FileBrowserIntent.Copy(state.selection, state.currentPath)) }) {
        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy here")
    }
    IconButton(onClick = { onIntent(FileBrowserIntent.Move(state.selection, state.currentPath)) }) {
        Icon(Icons.Filled.DriveFileMove, contentDescription = "Move here")
    }
    IconButton(onClick = { onIntent(FileBrowserIntent.Delete(state.selection)) }) {
        Icon(Icons.Filled.Delete, contentDescription = "Delete")
    }
}

@Composable
private fun SortMenu(
    expanded: Boolean,
    current: SortOrder,
    showHidden: Boolean,
    onDismiss: () -> Unit,
    onSort: (SortOrder) -> Unit,
    onToggleHidden: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        SortField.entries.forEach { field ->
            val selected = current.field == field
            DropdownMenuItem(
                text = { Text(sortLabel(field) + arrowFor(selected, current.ascending)) },
                onClick = {
                    val ascending = if (selected) !current.ascending else true
                    onSort(SortOrder(field, ascending))
                    onDismiss()
                },
            )
        }
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Filled.VisibilityOff, contentDescription = null) },
            text = { Text(if (showHidden) "Hide hidden files" else "Show hidden files") },
            onClick = {
                onToggleHidden()
                onDismiss()
            },
        )
    }
}

private fun sortLabel(field: SortField): String = when (field) {
    SortField.NAME -> "Name"
    SortField.SIZE -> "Size"
    SortField.DATE -> "Date"
}

private fun arrowFor(selected: Boolean, ascending: Boolean): String =
    if (!selected) "" else if (ascending) "  ↑" else "  ↓"
