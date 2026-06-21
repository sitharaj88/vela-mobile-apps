/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.presentation

import androidx.lifecycle.viewModelScope
import com.vela.apps.musicplayer.domain.AudioPlayer
import com.vela.apps.musicplayer.domain.PlaybackState
import com.vela.apps.musicplayer.domain.Track
import com.vela.core.common.MviStore
import kotlinx.coroutines.launch

/** How the player behaves when a track ends or [PlayerIntent.CycleRepeat] is invoked. */
enum class RepeatMode { Off, All, One }

data class PlayerState(
    val queue: List<Track> = emptyList(),
    val playback: PlaybackState = PlaybackState(),
    val shuffle: Boolean = false,
    val repeat: RepeatMode = RepeatMode.Off,
) {
    val currentTrack: Track? get() = playback.currentTrack
    val hasTrack: Boolean get() = currentTrack != null
}

sealed interface PlayerIntent {
    data class SetQueue(val tracks: List<Track>) : PlayerIntent
    data class PlayTrack(val track: Track) : PlayerIntent
    data object TogglePlayPause : PlayerIntent
    data object Next : PlayerIntent
    data object Prev : PlayerIntent
    data class Seek(val ms: Long) : PlayerIntent
    data object ToggleShuffle : PlayerIntent
    data object CycleRepeat : PlayerIntent
    data object Stop : PlayerIntent
}

/**
 * Drives [AudioPlayer]; the queue is the library list. Next/Prev walk through it honoring
 * [PlayerState.shuffle] and [PlayerState.repeat], and a finished track auto-advances the same way.
 */
class PlayerStore(private val audioPlayer: AudioPlayer) :
    MviStore<PlayerState, PlayerIntent, Nothing>(PlayerState()) {

    init {
        viewModelScope.launch {
            audioPlayer.state.collect { playback ->
                setState { copy(playback = playback) }
                if (playback.completed) onTrackCompleted()
            }
        }
    }

    override fun onIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.SetQueue -> setState { copy(queue = intent.tracks) }
            is PlayerIntent.PlayTrack -> audioPlayer.play(intent.track)
            PlayerIntent.TogglePlayPause -> togglePlayPause()
            PlayerIntent.Next -> step(forward = true)
            PlayerIntent.Prev -> step(forward = false)
            is PlayerIntent.Seek -> audioPlayer.seekTo(intent.ms)
            PlayerIntent.ToggleShuffle -> setState { copy(shuffle = !shuffle) }
            PlayerIntent.CycleRepeat -> setState { copy(repeat = repeat.next()) }
            PlayerIntent.Stop -> audioPlayer.stop()
        }
    }

    private fun togglePlayPause() {
        if (currentState.playback.isPlaying) audioPlayer.pause() else audioPlayer.resume()
    }

    private fun onTrackCompleted() {
        when (currentState.repeat) {
            RepeatMode.One -> currentState.currentTrack?.let(audioPlayer::play)
            RepeatMode.All -> step(forward = true)
            RepeatMode.Off -> if (!isLastInOrder()) step(forward = true) else audioPlayer.stop()
        }
    }

    private fun isLastInOrder(): Boolean {
        val queue = currentState.queue
        if (queue.isEmpty() || currentState.shuffle) return false
        val index = queue.indexOfFirst { it.id == currentState.playback.currentTrackId }
        return index == queue.lastIndex
    }

    private fun step(forward: Boolean) {
        val queue = currentState.queue
        if (queue.isEmpty()) return
        val currentId = currentState.playback.currentTrackId
        val index = queue.indexOfFirst { it.id == currentId }
        val nextTrack = when {
            currentState.shuffle -> pickShuffled(queue, index)
            index < 0 -> queue.first()
            forward -> queue[(index + 1) % queue.size]
            else -> queue[(index - 1 + queue.size) % queue.size]
        }
        audioPlayer.play(nextTrack)
    }

    private fun pickShuffled(queue: List<Track>, currentIndex: Int): Track {
        if (queue.size == 1) return queue.first()
        var pick = currentIndex
        while (pick == currentIndex) pick = queue.indices.random()
        return queue[pick]
    }

    override fun onCleared() {
        audioPlayer.release()
        super.onCleared()
    }
}

private fun RepeatMode.next(): RepeatMode = when (this) {
    RepeatMode.Off -> RepeatMode.All
    RepeatMode.All -> RepeatMode.One
    RepeatMode.One -> RepeatMode.Off
}
