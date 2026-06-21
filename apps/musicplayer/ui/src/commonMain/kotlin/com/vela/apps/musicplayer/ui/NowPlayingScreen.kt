/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vela.apps.musicplayer.presentation.PlayerState
import com.vela.apps.musicplayer.presentation.RepeatMode
import com.vela.core.designsystem.theme.LocalVelaTokens

/**
 * Full-screen Now Playing view: large artwork placeholder, title/artist/album, a draggable seek
 * bar with elapsed/remaining timestamps, transport controls, and shuffle + repeat toggles.
 */
@Composable
fun NowPlayingScreen(
    state: PlayerState,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = state.currentTrack
    val tokens = LocalVelaTokens.current
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = tokens.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.lg),
    ) {
        Spacer(Modifier.size(tokens.spacing.lg))
        Artwork(Modifier.fillMaxWidth(ARTWORK_WIDTH_FRACTION).aspectRatio(1f))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = track?.title ?: "Nothing playing",
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = listOfNotNull(track?.artist, track?.album).filter { it.isNotBlank() }
                    .joinToString(" • ").ifEmpty { "—" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        SeekBar(
            positionMs = state.playback.positionMs,
            durationMs = state.playback.durationMs,
            onSeek = onSeek,
        )
        TransportRow(
            isPlaying = state.playback.isPlaying,
            shuffle = state.shuffle,
            repeat = state.repeat,
            enabled = track != null,
            onToggle = onToggle,
            onNext = onNext,
            onPrev = onPrev,
            onToggleShuffle = onToggleShuffle,
            onCycleRepeat = onCycleRepeat,
        )
    }
}

@Composable
private fun Artwork(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = LocalVelaTokens.current.elevation.level2,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SeekBar(positionMs: Long, durationMs: Long, onSeek: (Long) -> Unit) {
    // While the user drags, show the dragged value; once released, fall back to live position.
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val range = durationMs.coerceAtLeast(1L).toFloat()
    val displayed = dragValue ?: positionMs.coerceIn(0, durationMs).toFloat()
    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = displayed.coerceIn(0f, range),
            onValueChange = { dragValue = it },
            onValueChangeFinished = {
                dragValue?.let { onSeek(it.toLong()) }
                dragValue = null
            },
            valueRange = 0f..range,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDuration(displayed.toLong()), style = MaterialTheme.typography.labelMedium)
            Text(formatDuration(durationMs), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun TransportRow(
    isPlaying: Boolean,
    shuffle: Boolean,
    repeat: RepeatMode,
    enabled: Boolean,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
) {
    val tokens = LocalVelaTokens.current
    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onToggleShuffle) {
            Icon(
                Icons.Filled.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffle) active else inactive,
            )
        }
        IconButton(onPrev, enabled = enabled) {
            Icon(Icons.Filled.SkipPrevious, "Previous", Modifier.size(tokens.spacing.xl))
        }
        IconButton(onToggle, enabled = enabled) {
            val icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow
            Icon(icon, if (isPlaying) "Pause" else "Play", Modifier.size(tokens.spacing.xxl))
        }
        IconButton(onNext, enabled = enabled) {
            Icon(Icons.Filled.SkipNext, "Next", Modifier.size(tokens.spacing.xl))
        }
        IconButton(onCycleRepeat) {
            val (icon, desc) = repeat.iconAndLabel()
            Icon(icon, desc, tint = if (repeat == RepeatMode.Off) inactive else active)
        }
    }
}

private fun RepeatMode.iconAndLabel(): Pair<ImageVector, String> = when (this) {
    RepeatMode.Off -> Icons.Filled.Repeat to "Repeat off"
    RepeatMode.All -> Icons.Filled.Repeat to "Repeat all"
    RepeatMode.One -> Icons.Filled.RepeatOne to "Repeat one"
}

private const val ARTWORK_WIDTH_FRACTION = 0.7f
