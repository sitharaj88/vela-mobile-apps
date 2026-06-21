/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.domain

import kotlinx.coroutines.flow.StateFlow

/** Immutable snapshot of what the player is currently doing. */
data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    /** True for the brief moment after a track finishes so consumers can auto-advance. */
    val completed: Boolean = false,
) {
    val currentTrackId: String? get() = currentTrack?.id
}

/** Plays a single [Track] at a time and reports progress through [state]. */
interface AudioPlayer {
    fun play(track: Track)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(ms: Long)
    fun release()
    val state: StateFlow<PlaybackState>
}
