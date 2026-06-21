/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.data

import com.vela.apps.musicplayer.domain.AudioPlayer
import com.vela.apps.musicplayer.domain.PlaybackState
import com.vela.apps.musicplayer.domain.Track
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

/**
 * `javax.sound.sampled.Clip` player. Plays uncompressed formats (WAV/AIFF/AU) reliably; compressed
 * formats (MP3/FLAC/OGG) are unsupported by the stock JVM SPI and stay silent while [state] still
 * advances the playhead so the UI behaves consistently. See the module summary for the optional
 * dependency that would add real MP3 support.
 */
internal class DesktopAudioPlayer : AudioPlayer {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var clip: Clip? = null
    private var ticker: Job? = null
    private var currentTrack: Track? = null
    private var fallbackDurationMs: Long = 0L

    @Suppress("TooGenericExceptionCaught")
    override fun play(track: Track) {
        teardownClip()
        currentTrack = track
        // Broad catch: the JVM throws several unrelated types (UnsupportedAudioFileException,
        // IOException, LineUnavailableException) for unplayable formats; all degrade to silent.
        val durationMs = try {
            openClip(track)?.also { it.start() }
            clip?.let { it.microsecondLength / MICROS_PER_MILLI } ?: track.durationMs
        } catch (error: Exception) {
            clip = null
            track.durationMs
        }
        fallbackDurationMs = durationMs
        _state.value = PlaybackState(track, isPlaying = true, positionMs = 0, durationMs = durationMs)
        startTicker()
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

    override fun stop() {
        teardownClip()
        currentTrack = null
        _state.value = PlaybackState()
    }

    override fun seekTo(ms: Long) {
        clip?.microsecondPosition = ms * MICROS_PER_MILLI
        _state.update { it.copy(positionMs = ms) }
    }

    override fun release() {
        teardownClip()
        currentTrack = null
        _state.value = PlaybackState()
        scope.coroutineContext[Job]?.cancel()
    }

    private fun openClip(track: Track): Clip? {
        val file = File(java.net.URI(track.uri))
        val stream = AudioSystem.getAudioInputStream(file)
        return AudioSystem.getClip().apply { open(stream); clip = this }
    }

    private fun teardownClip() {
        stopTicker()
        clip?.let { it.stop(); it.close() }
        clip = null
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val activeClip = clip
                val position = activeClip?.let { it.microsecondPosition / MICROS_PER_MILLI }
                    ?: (_state.value.positionMs + TICK_MS)
                val clamped = position.coerceAtMost(fallbackDurationMs)
                _state.update { it.copy(positionMs = clamped) }
                if (fallbackDurationMs > 0 && clamped >= fallbackDurationMs) {
                    _state.update { it.copy(isPlaying = false, completed = true) }
                    break
                }
                delay(TICK_MS)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private companion object {
        const val TICK_MS = 500L
        const val MICROS_PER_MILLI = 1000L
    }
}
