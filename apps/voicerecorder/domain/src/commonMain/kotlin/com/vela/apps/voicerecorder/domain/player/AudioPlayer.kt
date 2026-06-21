/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.domain.player

import kotlinx.coroutines.flow.StateFlow

/** Immutable snapshot of what the recording player is currently doing. */
data class PlaybackState(
    /** filePath of the recording being played, or null when idle. */
    val playingPath: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    /** True for the brief moment after playback finishes so the UI can reset. */
    val completed: Boolean = false,
)

/**
 * Plays back a single recording at a time and reports progress through [state]. Provided via
 * expect/actual: Android uses [android.media.MediaPlayer], desktop uses `javax.sound.sampled.Clip`,
 * iOS uses `AVAudioPlayer`.
 */
interface AudioPlayer {
    /** Live playback state (which file, playing flag, playhead, duration). */
    val state: StateFlow<PlaybackState>

    /** Loads [filePath] and starts playing from the beginning. */
    fun play(filePath: String, durationMs: Long)

    fun pause()
    fun resume()

    /** Seeks the current clip to [ms]. No-op if nothing is loaded. */
    fun seekTo(ms: Long)

    fun stop()

    /** Releases native resources. Called when the owning store is cleared. */
    fun release()
}
