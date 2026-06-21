/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.voicerecorder.domain.format.formatDuration
import com.vela.apps.voicerecorder.domain.format.formatSize
import com.vela.apps.voicerecorder.domain.model.Recording
import com.vela.apps.voicerecorder.presentation.RecorderEffect
import com.vela.apps.voicerecorder.presentation.RecorderIntent
import com.vela.apps.voicerecorder.presentation.RecorderState
import com.vela.apps.voicerecorder.presentation.RecorderStore
import com.vela.core.designsystem.component.VelaCard
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecorderScreen(
    store: RecorderStore = koinViewModel(),
) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(store) {
        store.effects.collect { effect ->
            when (effect) {
                is RecorderEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is RecorderEffect.ShareFile -> shareRecording(effect.filePath)
            }
        }
    }

    VelaScaffold(title = "Vela Recorder", snackbarHostState = snackbarHostState) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = tokens.spacing.lg)) {
            RecorderPanel(state = state, onIntent = store::onIntent)
            Spacer(Modifier.height(tokens.spacing.md))
            if (state.isEmpty) {
                VelaEmptyState(
                    icon = Icons.Filled.Mic,
                    title = "No recordings yet",
                    description = if (state.isSupported) {
                        "Tap the mic to capture your first recording."
                    } else {
                        "Recording isn't supported on this platform."
                    },
                )
            } else {
                RecordingsList(state = state, onIntent = store::onIntent)
            }
        }
    }

    state.renaming?.let { target ->
        RenameDialog(
            recording = target,
            onConfirm = { store.onIntent(RecorderIntent.ConfirmRename(it)) },
            onDismiss = { store.onIntent(RecorderIntent.DismissRename) },
        )
    }
}

@Composable
private fun RecorderPanel(state: RecorderState, onIntent: (RecorderIntent) -> Unit) {
    val tokens = LocalVelaTokens.current
    VelaCard(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatDuration(state.elapsedMs),
                style = MaterialTheme.typography.displaySmall,
                color = if (state.isRecording) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            AnimatedVisibility(visible = state.isRecording) {
                Waveform(
                    amplitudes = state.waveform,
                    modifier = Modifier.padding(vertical = tokens.spacing.sm),
                    color = if (state.isPaused) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
            Spacer(Modifier.height(tokens.spacing.sm))
            Row(
                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.isRecording) {
                    SmallControl(
                        icon = if (state.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        description = if (state.isPaused) "Resume" else "Pause",
                        onClick = { onIntent(RecorderIntent.TogglePause) },
                    )
                }
                RecordButton(
                    isRecording = state.isRecording,
                    enabled = state.isSupported,
                    onClick = { onIntent(RecorderIntent.ToggleRecord) },
                )
                if (state.isRecording) {
                    SmallControl(
                        icon = Icons.Filled.Delete,
                        description = "Discard",
                        onClick = { onIntent(RecorderIntent.CancelRecording) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordButton(isRecording: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val container = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant
        isRecording -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    Surface(color = container, shape = CircleShape, modifier = Modifier.size(80.dp)) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (isRecording) "Stop recording" else "Record",
                modifier = Modifier.size(36.dp),
                tint = if (isRecording) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
            )
        }
    }
}

@Composable
private fun SmallControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = CircleShape,
        modifier = Modifier.size(48.dp),
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun RecordingsList(state: RecorderState, onIntent: (RecorderIntent) -> Unit) {
    val tokens = LocalVelaTokens.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
    ) {
        items(state.recordings, key = { it.id }) { recording ->
            RecordingRow(
                recording = recording,
                isActive = state.playingId == recording.id,
                isPlaying = state.playback.isPlaying && state.playingId == recording.id,
                positionMs = if (state.playingId == recording.id) state.playback.positionMs else 0,
                onPlay = { onIntent(RecorderIntent.Play(recording)) },
                onTogglePlay = { onIntent(RecorderIntent.TogglePlayPause) },
                onSeek = { onIntent(RecorderIntent.Seek(it)) },
                onRename = { onIntent(RecorderIntent.RequestRename(recording)) },
                onShare = { onIntent(RecorderIntent.Share(recording)) },
                onDelete = { onIntent(RecorderIntent.Delete(recording.id)) },
            )
        }
    }
}

@Composable
private fun RecordingRow(
    recording: Recording,
    isActive: Boolean,
    isPlaying: Boolean,
    positionMs: Long,
    onPlay: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    val tokens = LocalVelaTokens.current
    VelaCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = if (isActive) onTogglePlay else onPlay) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Column(Modifier.weight(1f).padding(horizontal = tokens.spacing.md)) {
                    Text(
                        text = recording.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = rowSubtitle(recording),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRename) {
                    Icon(Icons.Filled.Edit, "Rename", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            AnimatedVisibility(visible = isActive) {
                val duration = recording.durationMs.coerceAtLeast(1)
                Column {
                    Slider(
                        value = positionMs.coerceIn(0, duration).toFloat(),
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..duration.toFloat(),
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatDuration(positionMs), style = MaterialTheme.typography.labelSmall)
                        Text(formatDuration(recording.durationMs), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private fun rowSubtitle(recording: Recording): String =
    formatDuration(recording.durationMs) + " • " + formatSize(recording.sizeBytes) +
        " • " + relativeTime(recording.createdAt)

@Composable
private fun RenameDialog(recording: Recording, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember(recording.id) { mutableStateOf(recording.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename recording") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("Name") },
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
