/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.vela.apps.musicplayer.presentation.PlayerState
import com.vela.core.designsystem.theme.LocalVelaTokens

/** Compact, tappable bottom bar: current track, a progress line, play/pause and next. */
@Composable
fun NowPlayingBar(
    state: PlayerState,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = state.currentTrack ?: return
    val tokens = LocalVelaTokens.current
    val playback = state.playback
    val progress = if (playback.durationMs > 0) {
        (playback.positionMs.toFloat() / playback.durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }
    Surface(modifier.fillMaxWidth(), tonalElevation = tokens.elevation.level2) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                Modifier
                    .clickable(onClick = onExpand)
                    .fillMaxWidth()
                    .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(tokens.spacing.xl).padding(end = tokens.spacing.sm),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onToggle) {
                    if (playback.isPlaying) {
                        Icon(Icons.Filled.Pause, "Pause")
                    } else {
                        Icon(Icons.Filled.PlayArrow, "Play")
                    }
                }
                IconButton(onNext) { Icon(Icons.Filled.SkipNext, "Next") }
            }
        }
    }
}
