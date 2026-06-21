/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.presentation

import app.cash.turbine.test
import com.vela.apps.voicerecorder.domain.model.Recording
import com.vela.apps.voicerecorder.domain.player.AudioPlayer
import com.vela.apps.voicerecorder.domain.player.PlaybackState
import com.vela.apps.voicerecorder.domain.recorder.AudioRecorder
import com.vela.apps.voicerecorder.domain.recorder.RecorderState as DomainRecorderState
import com.vela.apps.voicerecorder.domain.recorder.RecordingResult
import com.vela.apps.voicerecorder.domain.repository.RecordingRepository
import com.vela.apps.voicerecorder.domain.usecase.AddRecordingUseCase
import com.vela.apps.voicerecorder.domain.usecase.DeleteRecordingUseCase
import com.vela.apps.voicerecorder.domain.usecase.ObserveRecordingsUseCase
import com.vela.apps.voicerecorder.domain.usecase.RenameRecordingUseCase
import com.vela.core.common.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RecorderStoreTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private class TestDispatchers(private val d: CoroutineDispatcher) : DispatcherProvider {
        override val main = d
        override val default = d
        override val io = d
    }

    private class FakeRecorder(override val isSupported: Boolean = true) : AudioRecorder {
        private val _state = MutableStateFlow(DomainRecorderState())
        override val state = _state.asStateFlow()
        var nextResult: RecordingResult? = RecordingResult("/tmp/rec.m4a", 5_000, 1_024)
        var startCalls = 0
        var stopCalls = 0

        fun emit(value: DomainRecorderState) = _state.update { value }

        override suspend fun start() {
            startCalls++
            _state.update { DomainRecorderState(isRecording = true) }
        }
        override suspend fun pause() = _state.update { it.copy(isPaused = true) }
        override suspend fun resume() = _state.update { it.copy(isPaused = false) }
        override suspend fun stop(): RecordingResult? {
            stopCalls++
            _state.update { DomainRecorderState() }
            return nextResult
        }
    }

    private class FakePlayer : AudioPlayer {
        private val _state = MutableStateFlow(PlaybackState())
        override val state = _state.asStateFlow()
        var lastPlayedPath: String? = null
        var pauseCalls = 0
        var resumeCalls = 0
        var seekTo: Long? = null
        var stopCalls = 0

        override fun play(filePath: String, durationMs: Long) {
            lastPlayedPath = filePath
            _state.update { PlaybackState(playingPath = filePath, isPlaying = true, durationMs = durationMs) }
        }
        override fun pause() { pauseCalls++; _state.update { it.copy(isPlaying = false) } }
        override fun resume() { resumeCalls++; _state.update { it.copy(isPlaying = true) } }
        override fun seekTo(ms: Long) { seekTo = ms; _state.update { it.copy(positionMs = ms) } }
        override fun stop() { stopCalls++; _state.update { PlaybackState() } }
        override fun release() = stop()
    }

    private class FakeRepository : RecordingRepository {
        val items = MutableStateFlow<List<Recording>>(emptyList())
        override fun observeAll(): Flow<List<Recording>> = items
        override suspend fun add(name: String, filePath: String, durationMs: Long, sizeBytes: Long) {
            val id = (items.value.maxOfOrNull { it.id } ?: 0L) + 1L
            items.value = listOf(
                Recording(id, name, filePath, durationMs, sizeBytes, Clock.System.now()),
            ) + items.value
        }
        override suspend fun rename(id: Long, name: String) {
            items.value = items.value.map { if (it.id == id) it.copy(name = name) else it }
        }
        override suspend fun delete(id: Long) {
            items.value = items.value.filterNot { it.id == id }
        }
    }

    private lateinit var recorder: FakeRecorder
    private lateinit var player: FakePlayer
    private lateinit var repository: FakeRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        recorder = FakeRecorder()
        player = FakePlayer()
        repository = FakeRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun store(supported: Boolean = true): RecorderStore {
        if (!supported) recorder = FakeRecorder(isSupported = false)
        return RecorderStore(
            observeRecordings = ObserveRecordingsUseCase(repository),
            addRecording = AddRecordingUseCase(repository),
            renameRecording = RenameRecordingUseCase(repository),
            deleteRecording = DeleteRecordingUseCase(repository),
            recorder = recorder,
            player = player,
            dispatchers = TestDispatchers(dispatcher),
        )
    }

    @Test
    fun toggle_record_starts_capture_then_stops_and_persists() = runTest(dispatcher) {
        val store = store()

        store.onIntent(RecorderIntent.ToggleRecord)
        assertTrue(store.state.value.isRecording)
        assertEquals(1, recorder.startCalls)

        store.onIntent(RecorderIntent.ToggleRecord)
        assertFalse(store.state.value.isRecording)
        assertEquals(1, recorder.stopCalls)
        assertEquals(1, repository.items.value.size)
        assertEquals("Recording 001", repository.items.value.first().name)
        assertEquals(1_024, repository.items.value.first().sizeBytes)
    }

    @Test
    fun too_short_recording_is_not_persisted_and_emits_message() = runTest(dispatcher) {
        recorder.nextResult = RecordingResult("/tmp/x.m4a", durationMs = 100, sizeBytes = 10)
        val store = store()
        store.effects.test {
            store.onIntent(RecorderIntent.ToggleRecord)
            store.onIntent(RecorderIntent.ToggleRecord)
            val effect = awaitItem()
            assertTrue(effect is RecorderEffect.ShowMessage)
            assertTrue(repository.items.value.isEmpty())
        }
    }

    @Test
    fun unsupported_platform_emits_message_and_does_not_start() = runTest(dispatcher) {
        val store = store(supported = false)
        store.effects.test {
            store.onIntent(RecorderIntent.ToggleRecord)
            assertTrue(awaitItem() is RecorderEffect.ShowMessage)
            assertEquals(0, recorder.startCalls)
        }
    }

    @Test
    fun pause_and_resume_toggle_paused_flag() = runTest(dispatcher) {
        val store = store()
        store.onIntent(RecorderIntent.ToggleRecord)
        store.onIntent(RecorderIntent.TogglePause)
        assertTrue(store.state.value.isPaused)
        store.onIntent(RecorderIntent.TogglePause)
        assertFalse(store.state.value.isPaused)
    }

    @Test
    fun recorder_flow_drives_elapsed_and_waveform() = runTest(dispatcher) {
        val store = store()
        recorder.emit(DomainRecorderState(isRecording = true, elapsedMs = 2_000, amplitude = 0.5f))
        assertEquals(2_000, store.state.value.elapsedMs)
        assertEquals(listOf(0.5f), store.state.value.waveform)
    }

    @Test
    fun play_sets_active_recording_and_seek_forwards_to_player() = runTest(dispatcher) {
        repository.add("Recording 001", "/tmp/rec.m4a", 5_000, 1_024)
        val store = store()
        val recording = store.state.value.recordings.first()

        store.onIntent(RecorderIntent.Play(recording))
        assertEquals("/tmp/rec.m4a", player.lastPlayedPath)
        assertEquals(recording.id, store.state.value.playingId)

        store.onIntent(RecorderIntent.Seek(1_500))
        assertEquals(1_500, player.seekTo)

        store.onIntent(RecorderIntent.TogglePlayPause)
        assertEquals(1, player.pauseCalls)
    }

    @Test
    fun rename_dialog_flow_updates_repository() = runTest(dispatcher) {
        repository.add("Recording 001", "/tmp/rec.m4a", 5_000, 1_024)
        val store = store()
        val recording = store.state.value.recordings.first()

        store.onIntent(RecorderIntent.RequestRename(recording))
        assertEquals(recording.id, store.state.value.renaming?.id)

        store.onIntent(RecorderIntent.ConfirmRename("Interview"))
        assertNull(store.state.value.renaming)
        assertEquals("Interview", repository.items.value.first().name)
    }

    @Test
    fun delete_removes_recording_and_stops_playback_when_active() = runTest(dispatcher) {
        repository.add("Recording 001", "/tmp/rec.m4a", 5_000, 1_024)
        val store = store()
        val recording = store.state.value.recordings.first()
        store.onIntent(RecorderIntent.Play(recording))

        store.onIntent(RecorderIntent.Delete(recording.id))
        assertEquals(1, player.stopCalls)
        assertTrue(repository.items.value.isEmpty())
    }

    @Test
    fun share_emits_share_file_effect() = runTest(dispatcher) {
        repository.add("Recording 001", "/tmp/rec.m4a", 5_000, 1_024)
        val store = store()
        val recording = store.state.value.recordings.first()
        store.effects.test {
            store.onIntent(RecorderIntent.Share(recording))
            val effect = awaitItem()
            assertTrue(effect is RecorderEffect.ShareFile)
            assertEquals("/tmp/rec.m4a", (effect as RecorderEffect.ShareFile).filePath)
        }
    }
}
