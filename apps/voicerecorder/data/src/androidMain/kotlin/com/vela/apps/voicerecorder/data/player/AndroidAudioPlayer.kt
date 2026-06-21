/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.player

import android.media.MediaPlayer
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

/** [MediaPlayer]-backed playback; a coroutine ticker mirrors the playhead into [state]. */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal class AndroidAudioPlayer : AudioPlayer {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: MediaPlayer? = null
    private var ticker: Job? = null

    override fun play(filePath: String, durationMs: Long) {
        player?.release()
        try {
            player = MediaPlayer().apply {
                setDataSource(filePath)
                setOnCompletionListener {
                    stopTicker()
                    _state.update { it.copy(isPlaying = false, completed = true) }
                }
                prepare()
                start()
            }
        } catch (error: Exception) {
            player = null
            _state.value = PlaybackState()
            return
        }
        _state.value = PlaybackState(
            playingPath = filePath,
            isPlaying = true,
            positionMs = 0,
            durationMs = player?.duration?.toLong()?.takeIf { it > 0 } ?: durationMs,
        )
        startTicker()
    }

    override fun pause() {
        runCatching { player?.pause() }
        stopTicker()
        _state.update { it.copy(isPlaying = false) }
    }

    override fun resume() {
        runCatching { player?.start() }
        _state.update { it.copy(isPlaying = true, completed = false) }
        startTicker()
    }

    override fun seekTo(ms: Long) {
        runCatching { player?.seekTo(ms.toInt()) }
        _state.update { it.copy(positionMs = ms) }
    }

    override fun stop() {
        stopTicker()
        runCatching { player?.stop() }
        player?.release()
        player = null
        _state.value = PlaybackState()
    }

    override fun release() = stop()

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val current = player ?: break
                val pos = runCatching { current.currentPosition.toLong() }.getOrDefault(0L)
                _state.update { it.copy(positionMs = pos) }
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
    }
}
