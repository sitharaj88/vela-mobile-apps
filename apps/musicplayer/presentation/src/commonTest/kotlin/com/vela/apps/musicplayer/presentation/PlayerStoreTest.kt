/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerStoreTest {

    private val dispatcher = StandardTestDispatcher()
    private val tracks = listOf(sampleTrack("1"), sampleTrack("2"), sampleTrack("3"))

    private lateinit var player: FakeAudioPlayer
    private lateinit var store: PlayerStore

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        player = FakeAudioPlayer()
        store = PlayerStore(player)
        store.onIntent(PlayerIntent.SetQueue(tracks))
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun play_track_mirrors_playback_into_state() = runTest(dispatcher) {
        store.onIntent(PlayerIntent.PlayTrack(tracks[0]))
        testScheduler.advanceUntilIdle()

        assertEquals(tracks[0], store.state.value.currentTrack)
        assertTrue(store.state.value.playback.isPlaying)
    }

    @Test
    fun toggle_play_pause_delegates_to_player() = runTest(dispatcher) {
        store.onIntent(PlayerIntent.PlayTrack(tracks[0]))
        testScheduler.advanceUntilIdle()

        store.onIntent(PlayerIntent.TogglePlayPause)
        assertEquals(1, player.pauseCount)
        testScheduler.advanceUntilIdle()

        store.onIntent(PlayerIntent.TogglePlayPause)
        assertEquals(1, player.resumeCount)
    }

    @Test
    fun next_wraps_to_start_of_queue() = runTest(dispatcher) {
        store.onIntent(PlayerIntent.PlayTrack(tracks[2]))
        testScheduler.advanceUntilIdle()

        store.onIntent(PlayerIntent.Next)
        assertEquals("1", player.played.last().id)
    }

    @Test
    fun prev_wraps_to_end_of_queue() = runTest(dispatcher) {
        store.onIntent(PlayerIntent.PlayTrack(tracks[0]))
        testScheduler.advanceUntilIdle()

        store.onIntent(PlayerIntent.Prev)
        assertEquals("3", player.played.last().id)
    }

    @Test
    fun seek_delegates_to_player() = runTest(dispatcher) {
        store.onIntent(PlayerIntent.Seek(4200))
        assertEquals(4200, player.lastSeekMs)
    }

    @Test
    fun cycle_repeat_walks_off_all_one_off() = runTest(dispatcher) {
        assertEquals(RepeatMode.Off, store.state.value.repeat)
        store.onIntent(PlayerIntent.CycleRepeat)
        assertEquals(RepeatMode.All, store.state.value.repeat)
        store.onIntent(PlayerIntent.CycleRepeat)
        assertEquals(RepeatMode.One, store.state.value.repeat)
        store.onIntent(PlayerIntent.CycleRepeat)
        assertEquals(RepeatMode.Off, store.state.value.repeat)
    }

    @Test
    fun toggle_shuffle_flips_flag() = runTest(dispatcher) {
        assertTrue(!store.state.value.shuffle)
        store.onIntent(PlayerIntent.ToggleShuffle)
        assertTrue(store.state.value.shuffle)
    }

    @Test
    fun completion_with_repeat_off_at_last_track_stops() = runTest(dispatcher) {
        store.onIntent(PlayerIntent.PlayTrack(tracks[2]))
        testScheduler.advanceUntilIdle()

        player.signalCompleted()
        testScheduler.advanceUntilIdle()

        assertEquals(1, player.stopCount)
    }

    @Test
    fun completion_with_repeat_off_mid_queue_advances() = runTest(dispatcher) {
        store.onIntent(PlayerIntent.PlayTrack(tracks[0]))
        testScheduler.advanceUntilIdle()

        player.signalCompleted()
        testScheduler.advanceUntilIdle()

        assertEquals("2", player.played.last().id)
    }

    @Test
    fun completion_with_repeat_one_replays_same_track() = runTest(dispatcher) {
        store.onIntent(PlayerIntent.CycleRepeat) // All
        store.onIntent(PlayerIntent.CycleRepeat) // One
        store.onIntent(PlayerIntent.PlayTrack(tracks[1]))
        testScheduler.advanceUntilIdle()

        player.signalCompleted()
        testScheduler.advanceUntilIdle()

        assertEquals("2", player.played.last().id)
        assertEquals(2, player.played.count { it.id == "2" })
    }

    @Test
    fun completion_with_repeat_all_at_last_track_wraps() = runTest(dispatcher) {
        store.onIntent(PlayerIntent.CycleRepeat) // All
        store.onIntent(PlayerIntent.PlayTrack(tracks[2]))
        testScheduler.advanceUntilIdle()

        player.signalCompleted()
        testScheduler.advanceUntilIdle()

        assertEquals("1", player.played.last().id)
    }

    @Test
    fun cleared_releases_player() = runTest(dispatcher) {
        store.onIntent(PlayerIntent.Stop)
        assertEquals(1, player.stopCount)
    }
}
