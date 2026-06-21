/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.data

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
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

/** [MediaPlayer]-backed player; a coroutine tick mirrors the playhead into [state]. */
internal class AndroidAudioPlayer(private val context: Context) : AudioPlayer {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: MediaPlayer? = null
    private var ticker: Job? = null

    override fun play(track: Track) {
        player?.release()
        player = MediaPlayer().apply {
            setDataSource(context, Uri.parse(track.uri))
            setOnCompletionListener {
                stopTicker()
                _state.update { it.copy(isPlaying = false, completed = true) }
            }
            prepare()
            start()
        }
        _state.value = PlaybackState(
            currentTrack = track,
            isPlaying = true,
            positionMs = 0,
            durationMs = player?.duration?.toLong() ?: track.durationMs,
        )
        startTicker()
    }

    override fun pause() {
        player?.pause()
        stopTicker()
        _state.update { it.copy(isPlaying = false) }
    }

    override fun resume() {
        player?.start()
        _state.update { it.copy(isPlaying = true, completed = false) }
        startTicker()
    }

    override fun stop() {
        stopTicker()
        player?.release()
        player = null
        _state.value = PlaybackState()
    }

    override fun seekTo(ms: Long) {
        player?.seekTo(ms.toInt())
        _state.update { it.copy(positionMs = ms) }
    }

    override fun release() {
        stop()
        scope.coroutineContext[Job]?.cancel()
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val current = player ?: break
                _state.update { it.copy(positionMs = current.currentPosition.toLong()) }
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
    }
}
