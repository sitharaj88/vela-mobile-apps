/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.musicplayer.presentation.LibraryIntent
import com.vela.apps.musicplayer.presentation.LibraryStore
import com.vela.apps.musicplayer.presentation.PlayerIntent
import com.vela.apps.musicplayer.presentation.PlayerStore
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable for the Music Player. Both stores are hoisted here so playback survives
 * recomposition; the library's track list is pushed into the player as its play queue. A boolean
 * toggles between the library (with mini-player) and the full Now Playing screen.
 */
@Composable
fun MusicApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Crimson, themeMode = themeMode, dynamicColor = true) {
        val libraryStore: LibraryStore = koinViewModel()
        val playerStore: PlayerStore = koinViewModel()
        val libraryState by libraryStore.state.collectAsStateWithLifecycle()
        val playerState by playerStore.state.collectAsStateWithLifecycle()
        var showNowPlaying by remember { mutableStateOf(false) }

        LaunchedEffect(libraryState.tracks) {
            playerStore.onIntent(PlayerIntent.SetQueue(libraryState.tracks))
        }
        // Never strand the user on an empty Now Playing screen.
        LaunchedEffect(playerState.hasTrack) {
            if (!playerState.hasTrack) showNowPlaying = false
        }

        if (showNowPlaying) {
            VelaScaffold(
                title = "Now Playing",
                navigationIcon = Icons.Filled.KeyboardArrowDown,
                onNavigationClick = { showNowPlaying = false },
            ) { padding ->
                NowPlayingScreen(
                    state = playerState,
                    onToggle = { playerStore.onIntent(PlayerIntent.TogglePlayPause) },
                    onNext = { playerStore.onIntent(PlayerIntent.Next) },
                    onPrev = { playerStore.onIntent(PlayerIntent.Prev) },
                    onSeek = { ms -> playerStore.onIntent(PlayerIntent.Seek(ms)) },
                    onToggleShuffle = { playerStore.onIntent(PlayerIntent.ToggleShuffle) },
                    onCycleRepeat = { playerStore.onIntent(PlayerIntent.CycleRepeat) },
                    modifier = Modifier.fillMaxSize().padding(padding),
                )
            }
        } else {
            VelaScaffold(title = "Vela Music") { padding ->
                Column(Modifier.fillMaxSize().padding(padding)) {
                    LibraryScreen(
                        state = libraryState,
                        onSelectTab = { tab -> libraryStore.onIntent(LibraryIntent.SelectTab(tab)) },
                        currentTrackId = playerState.playback.currentTrackId,
                        onPlay = { track -> playerStore.onIntent(PlayerIntent.PlayTrack(track)) },
                        modifier = Modifier.weight(1f),
                    )
                    AnimatedVisibility(
                        visible = playerState.hasTrack,
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it },
                    ) {
                        NowPlayingBar(
                            state = playerState,
                            onToggle = { playerStore.onIntent(PlayerIntent.TogglePlayPause) },
                            onNext = { playerStore.onIntent(PlayerIntent.Next) },
                            onExpand = { showNowPlaying = true },
                        )
                    }
                }
            }
        }
    }
}
