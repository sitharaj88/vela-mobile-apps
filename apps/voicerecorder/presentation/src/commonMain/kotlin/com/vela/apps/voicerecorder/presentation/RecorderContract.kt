/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.presentation

import com.vela.apps.voicerecorder.domain.model.Recording
import com.vela.apps.voicerecorder.domain.player.PlaybackState

data class RecorderState(
    val recordings: List<Recording> = emptyList(),
    val isSupported: Boolean = true,
    val isLoading: Boolean = true,
    // Capture
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedMs: Long = 0,
    /** Normalized 0f..1f input level for the live meter. */
    val amplitude: Float = 0f,
    /** Rolling window of recent amplitudes for the waveform, oldest first. */
    val waveform: List<Float> = emptyList(),
    // Playback
    val playback: PlaybackState = PlaybackState(),
    // Inline rename dialog
    val renaming: Recording? = null,
) {
    val isEmpty: Boolean get() = !isLoading && recordings.isEmpty() && !isRecording
    val playingId: Long?
        get() = recordings.firstOrNull { it.filePath == playback.playingPath }?.id
}

sealed interface RecorderIntent {
    /** Starts capture if idle, otherwise stops and saves. */
    data object ToggleRecord : RecorderIntent

    /** Pauses an active capture, or resumes a paused one. */
    data object TogglePause : RecorderIntent

    /** Discards the in-flight capture without saving. */
    data object CancelRecording : RecorderIntent

    data class Play(val recording: Recording) : RecorderIntent
    data object TogglePlayPause : RecorderIntent
    data class Seek(val ms: Long) : RecorderIntent
    data object StopPlayback : RecorderIntent

    data class RequestRename(val recording: Recording) : RecorderIntent
    data class ConfirmRename(val name: String) : RecorderIntent
    data object DismissRename : RecorderIntent

    data class Delete(val id: Long) : RecorderIntent
    data class Share(val recording: Recording) : RecorderIntent
}

sealed interface RecorderEffect {
    /** Surfaced as a snackbar (permission denial, capture failure, unsupported platform). */
    data class ShowMessage(val message: String) : RecorderEffect

    /** Asks the platform layer to share the recording at [filePath] (no-op where unsupported). */
    data class ShareFile(val filePath: String) : RecorderEffect
}
