/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.presentation

import androidx.lifecycle.viewModelScope
import com.vela.apps.voicerecorder.domain.player.AudioPlayer
import com.vela.apps.voicerecorder.domain.recorder.AudioRecorder
import com.vela.apps.voicerecorder.domain.recorder.RecordingResult
import com.vela.apps.voicerecorder.domain.usecase.AddRecordingUseCase
import com.vela.apps.voicerecorder.domain.usecase.DeleteRecordingUseCase
import com.vela.apps.voicerecorder.domain.usecase.ObserveRecordingsUseCase
import com.vela.apps.voicerecorder.domain.usecase.RenameRecordingUseCase
import com.vela.core.common.DispatcherProvider
import com.vela.core.common.MviStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Single store driving the whole Voice Recorder screen: capture (start/pause/resume/stop with live
 * elapsed time + amplitude/waveform), the persisted recordings list, and playback (play/pause/seek)
 * plus rename / delete / share. Capture and playback state are folded in from the platform
 * [AudioRecorder] / [AudioPlayer] flows; list rows come from Room via [observeRecordings].
 */
class RecorderStore(
    private val observeRecordings: ObserveRecordingsUseCase,
    private val addRecording: AddRecordingUseCase,
    private val renameRecording: RenameRecordingUseCase,
    private val deleteRecording: DeleteRecordingUseCase,
    private val recorder: AudioRecorder,
    private val player: AudioPlayer,
    private val dispatchers: DispatcherProvider,
) : MviStore<RecorderState, RecorderIntent, RecorderEffect>(
    RecorderState(isSupported = recorder.isSupported),
) {

    init {
        viewModelScope.launch {
            observeRecordings().collect { recordings ->
                setState { copy(recordings = recordings, isLoading = false) }
            }
        }
        viewModelScope.launch {
            recorder.state.collect { rec ->
                setState {
                    copy(
                        isRecording = rec.isRecording,
                        isPaused = rec.isPaused,
                        elapsedMs = rec.elapsedMs,
                        amplitude = rec.amplitude,
                        waveform = if (rec.isRecording) {
                            (waveform + rec.amplitude).takeLast(WAVEFORM_SAMPLES)
                        } else {
                            emptyList()
                        },
                    )
                }
            }
        }
        viewModelScope.launch {
            player.state.collect { playback -> setState { copy(playback = playback) } }
        }
    }

    override fun onIntent(intent: RecorderIntent) {
        when (intent) {
            RecorderIntent.ToggleRecord -> toggleRecord()
            RecorderIntent.TogglePause -> togglePause()
            RecorderIntent.CancelRecording -> cancelRecording()
            is RecorderIntent.Play -> player.play(intent.recording.filePath, intent.recording.durationMs)
            RecorderIntent.TogglePlayPause -> togglePlayPause()
            is RecorderIntent.Seek -> player.seekTo(intent.ms)
            RecorderIntent.StopPlayback -> player.stop()
            is RecorderIntent.RequestRename -> setState { copy(renaming = intent.recording) }
            is RecorderIntent.ConfirmRename -> confirmRename(intent.name)
            RecorderIntent.DismissRename -> setState { copy(renaming = null) }
            is RecorderIntent.Delete -> delete(intent.id)
            is RecorderIntent.Share -> emitEffect(RecorderEffect.ShareFile(intent.recording.filePath))
        }
    }

    private fun toggleRecord() {
        if (!recorder.isSupported) {
            emitEffect(RecorderEffect.ShowMessage("Recording isn't supported on this platform."))
            return
        }
        if (currentState.isRecording) stopAndSave() else startRecording()
    }

    // Broad catch is intentional: the platform recorder reports failures via several unrelated
    // exception types, all surfaced to the user the same way.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun startRecording() {
        player.stop()
        viewModelScope.launch {
            try {
                recorder.start()
            } catch (error: Exception) {
                emitEffect(RecorderEffect.ShowMessage("Couldn't start recording. Check microphone permission."))
            }
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun stopAndSave() {
        viewModelScope.launch {
            val result = try {
                recorder.stop()
            } catch (error: Exception) {
                null
            }
            if (result == null || result.durationMs < MIN_DURATION_MS) {
                emitEffect(RecorderEffect.ShowMessage("Recording was too short to save."))
                return@launch
            }
            persist(result)
        }
    }

    private suspend fun persist(result: RecordingResult) {
        val name = defaultName(currentState.recordings.size + 1)
        withContext(dispatchers.io) {
            addRecording(name, result.filePath, result.durationMs, result.sizeBytes)
        }
    }

    private fun togglePause() {
        viewModelScope.launch {
            if (currentState.isPaused) recorder.resume() else recorder.pause()
        }
    }

    private fun cancelRecording() {
        viewModelScope.launch { runCatching { recorder.stop() } }
    }

    private fun togglePlayPause() {
        if (currentState.playback.isPlaying) player.pause() else player.resume()
    }

    private fun confirmRename(name: String) {
        val target = currentState.renaming ?: return
        setState { copy(renaming = null) }
        viewModelScope.launch {
            withContext(dispatchers.io) { renameRecording(target.id, name) }
        }
    }

    private fun delete(id: Long) {
        if (currentState.playingId == id) player.stop()
        viewModelScope.launch {
            withContext(dispatchers.io) { deleteRecording(id) }
        }
    }

    private fun defaultName(index: Int): String = "Recording ${index.toString().padStart(3, '0')}"

    override fun onCleared() {
        player.release()
        super.onCleared()
    }

    private companion object {
        const val WAVEFORM_SAMPLES = 48
        const val MIN_DURATION_MS = 500L
    }
}
