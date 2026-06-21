/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.notes.domain.model.Note
import com.vela.apps.notes.domain.model.NoteColor
import com.vela.apps.notes.domain.model.NoteSort
import com.vela.apps.notes.domain.model.NoteType
import com.vela.apps.notes.presentation.list.NotesLayout
import com.vela.apps.notes.presentation.list.NotesListEffect
import com.vela.apps.notes.presentation.list.NotesListIntent
import com.vela.apps.notes.presentation.list.NotesListStore
import com.vela.core.designsystem.component.VelaCard
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesListScreen(
    onOpenNote: (Long, Boolean) -> Unit,
    store: NotesListStore = koinViewModel(),
) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current

    LaunchedEffect(store) {
        store.effects.collect { effect ->
            when (effect) {
                is NotesListEffect.OpenEditor -> onOpenNote(effect.noteId, effect.checklist)
            }
        }
    }

    VelaScaffold(
        title = if (state.showArchived) "Archived" else "Notes",
        actions = { NotesListActions(state.layout, state.sort, state.showArchived, store) },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.md)) {
                FloatingActionButton(onClick = { store.onIntent(NotesListIntent.CreateChecklist) }) {
                    Icon(Icons.Filled.Checklist, contentDescription = "New checklist")
                }
                FloatingActionButton(onClick = { store.onIntent(NotesListIntent.CreateNote) }) {
                    Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = "New note")
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = tokens.spacing.lg)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { store.onIntent(NotesListIntent.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = tokens.spacing.sm),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Search notes") },
            )

            if (state.tags.isNotEmpty()) {
                TagFilterRow(state.tags, state.selectedTag) { tag ->
                    store.onIntent(NotesListIntent.TagSelected(tag))
                }
            }

            if (state.isEmpty) {
                EmptyNotes(state.showArchived, state.isFiltering)
            } else {
                NotesGrid(state.notes, state.layout, store, onOpenNote)
            }
        }
    }
}

@Composable
private fun NotesListActions(
    layout: NotesLayout,
    sort: NoteSort,
    showArchived: Boolean,
    store: NotesListStore,
) {
    IconButton(onClick = { store.onIntent(NotesListIntent.ToggleLayout) }) {
        val icon = if (layout == NotesLayout.List) Icons.Filled.GridView else Icons.Filled.ViewAgenda
        Icon(icon, contentDescription = "Toggle layout")
    }
    SortMenu(sort) { store.onIntent(NotesListIntent.SortChanged(it)) }
    IconButton(onClick = { store.onIntent(NotesListIntent.ToggleArchivedView) }) {
        val icon = if (showArchived) Icons.Filled.Unarchive else Icons.Filled.Archive
        Icon(icon, contentDescription = "Toggle archived")
    }
}

@Composable
private fun SortMenu(current: NoteSort, onSelect: (NoteSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.Sort, contentDescription = "Sort")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        NoteSort.entries.forEach { option ->
            DropdownMenuItem(
                text = { Text(sortLabel(option) + if (option == current) "  ✓" else "") },
                onClick = {
                    onSelect(option)
                    expanded = false
                },
            )
        }
    }
}

private fun sortLabel(sort: NoteSort): String = when (sort) {
    NoteSort.Modified -> "Last modified"
    NoteSort.Created -> "Date created"
    NoteSort.Title -> "Title"
}

@Composable
private fun TagFilterRow(tags: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    val tokens = LocalVelaTokens.current
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = tokens.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("All") },
            )
        }
        items(tags) { tag ->
            FilterChip(
                selected = selected == tag,
                onClick = { onSelect(if (selected == tag) null else tag) },
                label = { Text(tag) },
            )
        }
    }
}

@Composable
private fun EmptyNotes(showArchived: Boolean, isFiltering: Boolean) {
    VelaEmptyState(
        icon = Icons.Filled.Description,
        title = when {
            isFiltering -> "No matches"
            showArchived -> "Nothing archived"
            else -> "No notes yet"
        },
        description = when {
            isFiltering -> "Try a different search or tag."
            showArchived -> "Archived notes will appear here."
            else -> "Tap + to write your first note."
        },
    )
}

@Composable
private fun NotesGrid(
    notes: List<Note>,
    layout: NotesLayout,
    store: NotesListStore,
    onOpenNote: (Long, Boolean) -> Unit,
) {
    val tokens = LocalVelaTokens.current
    val columns = if (layout == NotesLayout.Grid) 2 else 1
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
        contentPadding = PaddingValues(vertical = tokens.spacing.sm),
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(
                note = note,
                onClick = { onOpenNote(note.id, false) },
                onTogglePin = { store.onIntent(NotesListIntent.TogglePin(note.id, !note.pinned)) },
                onToggleArchive = {
                    store.onIntent(NotesListIntent.ToggleArchive(note.id, !note.archived))
                },
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
) {
    val tokens = LocalVelaTokens.current
    VelaCard(
        modifier = Modifier.fillMaxWidth()
            .background(note.color.containerColor())
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (note.color != NoteColor.None) {
                Box(Modifier.size(10.dp).background(note.color.swatch(), CircleShape))
            }
            Text(
                text = note.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = tokens.spacing.xs),
            )
            IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.PushPin,
                    contentDescription = if (note.pinned) "Unpin" else "Pin",
                    tint = if (note.pinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            IconButton(onClick = onToggleArchive, modifier = Modifier.size(32.dp)) {
                val icon = if (note.archived) Icons.Filled.Unarchive else Icons.Filled.Archive
                Icon(
                    icon,
                    contentDescription = if (note.archived) "Unarchive" else "Archive",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        NotePreview(note)
        if (note.tags.isNotEmpty()) {
            Text(
                text = note.tags.joinToString(" ") { "#$it" },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = tokens.spacing.xs),
            )
        }
    }
}

@Composable
private fun NotePreview(note: Note) {
    val tokens = LocalVelaTokens.current
    if (note.type == NoteType.CHECKLIST) {
        Text(
            text = "${note.checkedCount}/${note.items.size} done",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        note.items.take(MAX_PREVIEW_ITEMS).forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CheckBox,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (item.checked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = tokens.spacing.xs),
                )
            }
        }
    } else if (note.content.isNotBlank()) {
        Text(
            text = note.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val MAX_PREVIEW_ITEMS = 4
