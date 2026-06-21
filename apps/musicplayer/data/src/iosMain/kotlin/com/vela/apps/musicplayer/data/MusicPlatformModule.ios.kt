/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.data

import com.vela.apps.musicplayer.domain.AudioPlayer
import com.vela.apps.musicplayer.domain.MusicLibrary
import com.vela.apps.musicplayer.domain.PlaybackState
import com.vela.apps.musicplayer.domain.Track
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL

/** iOS player wiring. The library is a best-effort stub; the player is a best-effort [AVPlayer]. */
actual fun musicPlatformModule(): Module = module {
    single<MusicLibrary> { IosMusicLibrary() }
    single<AudioPlayer> { IosAudioPlayer() }
}

/**
 * TODO(ios): query the on-device media library via `MPMediaQuery.songsQuery()` once the
 * `NSAppleMusicUsageDescription` entitlement is configured and cinterop for MediaPlayer is wired.
 * Returns empty for now so the app still builds and runs on a simulator without media access.
 */
private class IosMusicLibrary : MusicLibrary {
    override suspend fun queryTracks(): List<Track> = emptyList()
}

/**
 * Best-effort [AVPlayer] wrapper. State transitions are tracked locally; position ticking via a
 * periodic time observer is deferred. TODO(ios): wire `addPeriodicTimeObserverForInterval` to drive
 * [PlaybackState.positionMs] and an `AVPlayerItemDidPlayToEndTime` observer for [completed].
 */
@OptIn(ExperimentalForeignApi::class)
private class IosAudioPlayer : AudioPlayer {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var player: AVPlayer? = null

    override fun play(track: Track) {
        val url = NSURL.URLWithString(track.uri)
        player = url?.let { AVPlayer(uRL = it) }
        player?.play()
        _state.value = PlaybackState(track, isPlaying = true, positionMs = 0, durationMs = track.durationMs)
    }

    override fun pause() {
        player?.pause()
        _state.update { it.copy(isPlaying = false) }
    }

    override fun resume() {
        player?.play()
        _state.update { it.copy(isPlaying = true, completed = false) }
    }

    override fun stop() {
        player?.pause()
        player = null
        _state.value = PlaybackState()
    }

    override fun seekTo(ms: Long) {
        val seconds = ms / MILLIS_PER_SECOND.toDouble()
        player?.seekToTime(CMTimeMakeWithSeconds(seconds, PREFERRED_TIMESCALE))
        _state.update { it.copy(positionMs = ms) }
    }

    override fun release() {
        player?.pause()
        player = null
        _state.value = PlaybackState()
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000
        const val PREFERRED_TIMESCALE = 1_000_000
    }
}
