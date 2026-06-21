/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.presentation

import com.vela.apps.musicplayer.domain.AudioPlayer
import com.vela.apps.musicplayer.domain.MusicLibrary
import com.vela.apps.musicplayer.domain.PlaybackState
import com.vela.apps.musicplayer.domain.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal fun sampleTrack(
    id: String,
    title: String = "Track $id",
    artist: String = "Artist",
    album: String = "Album",
    durationMs: Long = 1000,
) = Track(id = id, uri = "file://$id", title = title, artist = artist, album = album, durationMs = durationMs)

/** In-memory [MusicLibrary] returning a fixed track list. */
internal class FakeMusicLibrary(private val tracks: List<Track>) : MusicLibrary {
    override suspend fun queryTracks(): List<Track> = tracks
}

/** [AudioPlayer] fake that records calls and lets tests drive the state flow. */
internal class FakeAudioPlayer : AudioPlayer {
    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    val played = mutableListOf<Track>()
    var pauseCount = 0
    var resumeCount = 0
    var stopCount = 0
    var releaseCount = 0
    var lastSeekMs: Long? = null

    override fun play(track: Track) {
        played += track
        _state.value = PlaybackState(currentTrack = track, isPlaying = true, durationMs = track.durationMs)
    }

    override fun pause() {
        pauseCount++
        _state.update { it.copy(isPlaying = false) }
    }

    override fun resume() {
        resumeCount++
        _state.update { it.copy(isPlaying = true) }
    }

    override fun stop() {
        stopCount++
        _state.value = PlaybackState()
    }

    override fun seekTo(ms: Long) {
        lastSeekMs = ms
        _state.update { it.copy(positionMs = ms) }
    }

    override fun release() {
        releaseCount++
    }

    /** Test helper: simulate the current track finishing. */
    fun signalCompleted() {
        _state.update { it.copy(isPlaying = false, completed = true) }
    }
}
