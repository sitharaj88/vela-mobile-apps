/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.domain.recorder

import kotlinx.coroutines.flow.StateFlow

/** Result of a finished recording: the output file path, its duration, and its size on disk. */
data class RecordingResult(
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
)

/** Live, observable state of an in-flight (or idle) capture session. */
data class RecorderState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    /** Elapsed captured time in millis (excludes paused spans). */
    val elapsedMs: Long = 0,
    /** Most recent input level, normalized 0f..1f, for the waveform/level meter. */
    val amplitude: Float = 0f,
)

/**
 * Platform audio capture. Provided via expect/actual:
 * - Android: [android.media.MediaRecorder] -> m4a/aac.
 * - Desktop: `javax.sound.sampled` TargetDataLine -> WAV.
 * - iOS: best-effort `AVAudioRecorder`.
 *
 * All transport methods are coroutine-friendly; [state] streams live elapsed time and amplitude so
 * the UI can render a record timer and level meter without polling.
 */
interface AudioRecorder {
    /** Whether this platform can capture audio at all. */
    val isSupported: Boolean

    /** Live capture state (recording flag, elapsed millis, amplitude). */
    val state: StateFlow<RecorderState>

    /** Begins capture into a fresh file. May throw if permission is missing or the mic is busy. */
    suspend fun start()

    /** Pauses capture; the elapsed timer freezes. No-op if not recording. */
    suspend fun pause()

    /** Resumes a paused capture. No-op if not paused. */
    suspend fun resume()

    /** Stops capture and returns the output file + duration + size, or null if nothing was recorded. */
    suspend fun stop(): RecordingResult?
}
