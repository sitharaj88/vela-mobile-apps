/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.vela.apps.voicerecorder.domain.recorder.AudioRecorder
import com.vela.apps.voicerecorder.domain.recorder.RecorderState
import com.vela.apps.voicerecorder.domain.recorder.RecordingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Captures AAC audio into `filesDir/recordings/<timestamp>.m4a` via [MediaRecorder]. Pause/resume use
 * the platform APIs (available on the app's minSdk 24). A coroutine ticker mirrors elapsed time and
 * the normalized input level into [state] for the timer + waveform.
 *
 * Fails gracefully (returns null / no-ops) when RECORD_AUDIO has not been granted, since
 * [MediaRecorder.start] throws in that case. Broad `catch (Exception)` is intentional: MediaRecorder
 * surfaces failures as several unrelated types (IllegalState / IO / Security / RuntimeException).
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class AndroidAudioRecorder(
    private val context: Context,
) : AudioRecorder {

    override val isSupported: Boolean = true

    private val _state = MutableStateFlow(RecorderState())
    override val state: StateFlow<RecorderState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var ticker: Job? = null

    /** Accumulated millis from completed (pre-pause) spans plus the active span's start marker. */
    private var accumulatedMs: Long = 0
    private var spanStartMs: Long = 0

    override suspend fun start() {
        runCatching { recorder?.release() }
        recorder = null

        val dir = File(context.filesDir, "recordings").apply { mkdirs() }
        val file = File(dir, "${System.currentTimeMillis()}.m4a")
        outputFile = file

        try {
            val mr = newRecorder()
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setAudioEncodingBitRate(BIT_RATE)
            mr.setAudioSamplingRate(SAMPLE_RATE)
            mr.setOutputFile(file.absolutePath)
            mr.prepare()
            mr.start()
            recorder = mr
            accumulatedMs = 0
            spanStartMs = System.currentTimeMillis()
            _state.value = RecorderState(isRecording = true, isPaused = false)
            startTicker()
        } catch (error: Exception) {
            runCatching { recorder?.release() }
            recorder = null
            outputFile = null
            _state.value = RecorderState()
            throw error
        }
    }

    override suspend fun pause() {
        val mr = recorder ?: return
        if (_state.value.isPaused) return
        try {
            mr.pause()
            accumulatedMs += (System.currentTimeMillis() - spanStartMs).coerceAtLeast(0)
            stopTicker()
            _state.update { it.copy(isPaused = true, amplitude = 0f, elapsedMs = accumulatedMs) }
        } catch (error: Exception) {
            // Pause unsupported on the device — leave the session recording.
        }
    }

    override suspend fun resume() {
        val mr = recorder ?: return
        if (!_state.value.isPaused) return
        try {
            mr.resume()
            spanStartMs = System.currentTimeMillis()
            _state.update { it.copy(isPaused = false) }
            startTicker()
        } catch (error: Exception) {
            // Resume unsupported — keep it paused.
        }
    }

    override suspend fun stop(): RecordingResult? {
        val mr = recorder ?: return null
        val file = outputFile
        val paused = _state.value.isPaused
        val durationMs = (accumulatedMs +
            if (paused) 0 else (System.currentTimeMillis() - spanStartMs)).coerceAtLeast(0)
        stopTicker()
        recorder = null
        outputFile = null
        _state.value = RecorderState()
        return try {
            mr.stop()
            mr.release()
            if (file != null && file.exists()) {
                RecordingResult(file.absolutePath, durationMs, file.length())
            } else {
                null
            }
        } catch (error: Exception) {
            runCatching { mr.release() }
            runCatching { file?.delete() }
            null
        }
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val mr = recorder ?: break
                val elapsed = accumulatedMs + (System.currentTimeMillis() - spanStartMs)
                val amp = runCatching { mr.maxAmplitude }.getOrDefault(0)
                _state.update {
                    it.copy(
                        elapsedMs = elapsed.coerceAtLeast(0),
                        amplitude = (amp / MAX_AMPLITUDE).coerceIn(0f, 1f),
                    )
                }
                delay(TICK_MS)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private fun newRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

    private companion object {
        const val TICK_MS = 100L
        const val MAX_AMPLITUDE = 32767f
        const val BIT_RATE = 128_000
        const val SAMPLE_RATE = 44_100
    }
}
