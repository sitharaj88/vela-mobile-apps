/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.player

import com.vela.apps.voicerecorder.domain.player.AudioPlayer
import com.vela.apps.voicerecorder.domain.player.PlaybackState
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
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

/**
 * `javax.sound.sampled.Clip` playback for the WAV files captured by the desktop recorder. Reports
 * the playhead through [state]; completion flips `completed` so the store can reset the row.
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal class DesktopAudioPlayer : AudioPlayer {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var clip: Clip? = null
    private var ticker: Job? = null

    override fun play(filePath: String, durationMs: Long) {
        closeClip()
        val resolvedDuration = try {
            val stream = AudioSystem.getAudioInputStream(File(filePath))
            val opened = AudioSystem.getClip().apply {
                open(stream)
                addLineListener { event -> if (event.type == LineEvent.Type.STOP) onLineStop() }
                start()
            }
            clip = opened
            (opened.microsecondLength / MICROS_PER_MILLI).takeIf { it > 0 } ?: durationMs
        } catch (error: Exception) {
            clip = null
            durationMs
        }
        _state.value = PlaybackState(
            playingPath = filePath,
            isPlaying = clip != null,
            positionMs = 0,
            durationMs = resolvedDuration,
        )
        if (clip != null) startTicker()
    }

    override fun pause() {
        clip?.stop()
        stopTicker()
        _state.update { it.copy(isPlaying = false) }
    }

    override fun resume() {
        clip?.start()
        _state.update { it.copy(isPlaying = true, completed = false) }
        startTicker()
    }

    override fun seekTo(ms: Long) {
        clip?.microsecondPosition = (ms * MICROS_PER_MILLI).coerceAtLeast(0)
        _state.update { it.copy(positionMs = ms) }
    }

    override fun stop() {
        stopTicker()
        closeClip()
        _state.value = PlaybackState()
    }

    override fun release() = stop()

    /** Fired by the line listener when the clip reaches its end (or is stopped). */
    private fun onLineStop() {
        val current = clip ?: return
        val atEnd = current.microsecondPosition >= current.microsecondLength
        if (atEnd) {
            stopTicker()
            _state.update { it.copy(isPlaying = false, completed = true) }
        }
    }

    private fun closeClip() {
        clip?.let { runCatching { it.stop(); it.close() } }
        clip = null
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val active = clip ?: break
                val pos = active.microsecondPosition / MICROS_PER_MILLI
                _state.update { it.copy(positionMs = pos.coerceAtLeast(0)) }
                delay(TICK_MS)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private companion object {
        const val TICK_MS = 200L
        const val MICROS_PER_MILLI = 1000L
    }
}
