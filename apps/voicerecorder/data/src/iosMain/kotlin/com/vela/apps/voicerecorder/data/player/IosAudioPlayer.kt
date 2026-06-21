/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.player

import com.vela.apps.voicerecorder.domain.player.AudioPlayer
import com.vela.apps.voicerecorder.domain.player.PlaybackState
import kotlinx.cinterop.ExperimentalForeignApi
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
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL

/**
 * Best-effort iOS playback via [AVAudioPlayer]. A coroutine ticker mirrors `currentTime` into
 * [state]. Completion handling is approximated by comparing position to duration since wiring an
 * AVAudioPlayerDelegate from Kotlin/Native needs on-device verification — marked `// TODO(ios)`.
 */
@OptIn(ExperimentalForeignApi::class)
@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal class IosAudioPlayer : AudioPlayer {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: AVAudioPlayer? = null
    private var ticker: Job? = null

    override fun play(filePath: String, durationMs: Long) {
        stop()
        val url = NSURL.fileURLWithPath(filePath)
        val avPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
        player = avPlayer
        avPlayer.prepareToPlay()
        avPlayer.play()
        val resolved = (avPlayer.duration * MILLIS_PER_SECOND).toLong().takeIf { it > 0 } ?: durationMs
        _state.value = PlaybackState(
            playingPath = filePath,
            isPlaying = true,
            positionMs = 0,
            durationMs = resolved,
        )
        startTicker()
    }

    override fun pause() {
        player?.pause()
        stopTicker()
        _state.update { it.copy(isPlaying = false) }
    }

    override fun resume() {
        player?.play()
        _state.update { it.copy(isPlaying = true, completed = false) }
        startTicker()
    }

    override fun seekTo(ms: Long) {
        player?.setCurrentTime(ms / MILLIS_PER_SECOND)
        _state.update { it.copy(positionMs = ms) }
    }

    override fun stop() {
        stopTicker()
        player?.stop()
        player = null
        _state.value = PlaybackState()
    }

    override fun release() = stop()

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val current = player ?: break
                val pos = (current.currentTime * MILLIS_PER_SECOND).toLong()
                val done = !current.playing && pos > 0
                _state.update {
                    it.copy(positionMs = pos.coerceAtLeast(0), isPlaying = current.playing, completed = done)
                }
                if (done) break
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
        const val MILLIS_PER_SECOND = 1000.0
    }
}
