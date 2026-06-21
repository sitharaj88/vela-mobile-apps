/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.notes.domain.model.ChecklistItem
import com.vela.apps.notes.domain.model.NoteColor
import com.vela.apps.notes.presentation.editor.NoteEditorEffect
import com.vela.apps.notes.presentation.editor.NoteEditorIntent
import com.vela.apps.notes.presentation.editor.NoteEditorState
import com.vela.apps.notes.presentation.editor.NoteEditorStore
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteEditorScreen(
    noteId: Long,
    checklist: Boolean,
    onBack: () -> Unit,
    store: NoteEditorStore = koinViewModel { parametersOf(noteId, checklist) },
) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current

    LaunchedEffect(store) {
        store.effects.collect { effect ->
            when (effect) {
                NoteEditorEffect.NavigateBack -> onBack()
            }
        }
    }

    val transparentColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
    )

    VelaScaffold(
        title = if (state.isNew) "New note" else "Edit note",
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = { store.onIntent(NoteEditorIntent.Close) },
        actions = { EditorActions(state, store) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = tokens.spacing.lg)) {
            TextField(
                value = state.title,
                onValueChange = { store.onIntent(NoteEditorIntent.TitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title", style = MaterialTheme.typography.titleLarge) },
                textStyle = MaterialTheme.typography.titleLarge,
                singleLine = true,
                colors = transparentColors,
            )
            ColorPickerRow(state.color) { store.onIntent(NoteEditorIntent.ColorChanged(it)) }
            TextField(
                value = state.tags.joinToString(", "),
                onValueChange = { store.onIntent(NoteEditorIntent.TagsChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tags, comma separated") },
                singleLine = true,
                colors = transparentColors,
            )

            if (state.isChecklist) {
                ChecklistEditor(state.items, store, transparentColors)
            } else {
                TextField(
                    value = state.content,
                    onValueChange = { store.onIntent(NoteEditorIntent.ContentChanged(it)) },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    placeholder = { Text("Write something…") },
                    colors = transparentColors,
                )
            }
        }
    }
}

@Composable
private fun EditorActions(state: NoteEditorState, store: NoteEditorStore) {
    IconButton(onClick = { store.onIntent(NoteEditorIntent.ToggleType) }) {
        val icon = if (state.isChecklist) Icons.Filled.Notes else Icons.Filled.Checklist
        Icon(icon, contentDescription = "Toggle note type")
    }
    IconButton(onClick = { store.onIntent(NoteEditorIntent.TogglePin) }) {
        Icon(
            Icons.Filled.PushPin,
            contentDescription = if (state.pinned) "Unpin" else "Pin",
            tint = if (state.pinned) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
    IconButton(onClick = { store.onIntent(NoteEditorIntent.ToggleArchive) }) {
        val icon = if (state.archived) Icons.Filled.Unarchive else Icons.Filled.Archive
        Icon(icon, contentDescription = if (state.archived) "Unarchive" else "Archive")
    }
    if (!state.isNew) {
        IconButton(onClick = { store.onIntent(NoteEditorIntent.Delete) }) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete note")
        }
    }
}

@Composable
private fun ColorPickerRow(selected: NoteColor, onSelect: (NoteColor) -> Unit) {
    val tokens = LocalVelaTokens.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = tokens.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NoteColor.entries.forEach { color ->
            val isSelected = color == selected
            val dotColor = if (color == NoteColor.None) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                color.swatch()
            }
            Box(
                modifier = Modifier.size(28.dp)
                    .background(dotColor, CircleShape)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape,
                    )
                    .clickable { onSelect(color) },
                contentAlignment = Alignment.Center,
            ) {
                if (color == NoteColor.None) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "No color",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistEditor(
    items: List<ChecklistItem>,
    store: NoteEditorStore,
    transparentColors: TextFieldColors,
) {
    val tokens = LocalVelaTokens.current
    LazyColumn(Modifier.fillMaxSize()) {
        items(items, key = { it.id }) { item ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.checked,
                    onCheckedChange = {
                        store.onIntent(NoteEditorIntent.ItemCheckedChanged(item.id, it))
                    },
                )
                TextField(
                    value = item.text,
                    onValueChange = {
                        store.onIntent(NoteEditorIntent.ItemTextChanged(item.id, it))
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("List item") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                    ),
                    colors = transparentColors,
                )
                IconButton(onClick = { store.onIntent(NoteEditorIntent.RemoveItem(item.id)) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove item")
                }
            }
        }
        item {
            TextButton(
                onClick = { store.onIntent(NoteEditorIntent.AddItem) },
                modifier = Modifier.padding(top = tokens.spacing.sm),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Add item", modifier = Modifier.padding(start = tokens.spacing.sm))
            }
        }
    }
}
