/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vela.apps.filemanager.domain.FileNode
import com.vela.apps.filemanager.presentation.Crumb
import com.vela.apps.filemanager.presentation.FileBrowserState
import com.vela.core.designsystem.theme.LocalVelaTokens

/** Horizontally scrollable breadcrumb path bar; each segment navigates to that directory. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BreadcrumbBar(crumbs: List<Crumb>, onNavigate: (String) -> Unit) {
    val tokens = LocalVelaTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.sm),
    ) {
        crumbs.forEachIndexed { index, crumb ->
            if (index > 0) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
            val isLast = index == crumbs.lastIndex
            Text(
                text = crumb.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .combinedClickable(onClick = { onNavigate(crumb.path) })
                    .padding(horizontal = tokens.spacing.xs, vertical = tokens.spacing.xs),
            )
        }
    }
}

/** Compact summary of the current folder's contents and total file size. */
@Composable
internal fun StorageHeader(state: FileBrowserState) {
    val tokens = LocalVelaTokens.current
    Text(
        text = "${state.folderCount} folders · ${state.fileCount} files · ${humanReadableSize(state.totalSizeBytes)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.xs),
    )
}

/** Inline search field with a clear/close action. */
@Composable
internal fun FileSearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val tokens = LocalVelaTokens.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = { Text("Search this folder") },
        trailingIcon = {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close search") }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.sm),
    )
}

/** The scrolling list of entries with click/long-press selection and a per-row overflow menu. */
@Composable
internal fun FileList(
    state: FileBrowserState,
    onOpen: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onRename: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    LazyColumn(Modifier.fillMaxWidth()) {
        items(state.visibleEntries, key = { it.path }) { node ->
            FileRow(
                node = node,
                selected = node.path in state.selection,
                selectionMode = state.inSelectionMode,
                onClick = {
                    if (state.inSelectionMode) onToggleSelect(node.path)
                    else if (node.isDirectory) onOpen(node.path)
                },
                onLongClick = { onToggleSelect(node.path) },
                onRename = { onRename(node.path) },
                onDelete = { onDelete(node.path) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(
    node: FileNode,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val tokens = LocalVelaTokens.current
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.md),
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.Check else iconFor(node),
            contentDescription = null,
            tint = if (node.isDirectory) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(40.dp),
        )
        Column(Modifier.weight(1f).padding(start = tokens.spacing.lg)) {
            Text(
                text = node.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitleFor(node),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        if (!selectionMode) {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Actions")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Rename") }, onClick = {
                        menuOpen = false
                        onRename()
                    })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = {
                        menuOpen = false
                        onDelete()
                    })
                    DropdownMenuItem(text = { Text("Select") }, onClick = {
                        menuOpen = false
                        onLongClick()
                    })
                }
            }
        }
    }
}

/** Single-field dialog used for new-folder and rename. */
@Composable
internal fun TextEntryDialog(
    title: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    initial: String = "",
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank()) onConfirm(text) }),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
