/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.presentation

import app.cash.turbine.test
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
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryStoreTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun load_sorts_tracks_and_derives_albums_and_artists() = runTest(dispatcher) {
        val library = FakeMusicLibrary(
            listOf(
                sampleTrack("1", title = "Zulu", artist = "A", album = "X"),
                sampleTrack("2", title = "Alpha", artist = "A", album = "X"),
                sampleTrack("3", title = "Mike", artist = "B", album = "Y"),
            ),
        )
        val store = LibraryStore(library)
        testScheduler.advanceUntilIdle()

        val state = store.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("Alpha", "Mike", "Zulu"), state.tracks.map { it.title })
        assertEquals(listOf("X", "Y"), state.albums.map { it.name })
        assertEquals(listOf("A", "B"), state.artists.map { it.name })
    }

    @Test
    fun select_tab_updates_state() = runTest(dispatcher) {
        val store = LibraryStore(FakeMusicLibrary(emptyList()))
        testScheduler.advanceUntilIdle()

        store.state.test {
            assertEquals(LibraryTab.Songs, awaitItem().tab)
            store.onIntent(LibraryIntent.SelectTab(LibraryTab.Albums))
            assertEquals(LibraryTab.Albums, awaitItem().tab)
        }
    }

    @Test
    fun reload_requeries_library() = runTest(dispatcher) {
        val store = LibraryStore(FakeMusicLibrary(listOf(sampleTrack("1"))))
        testScheduler.advanceUntilIdle()
        assertEquals(1, store.state.value.tracks.size)

        store.onIntent(LibraryIntent.Reload)
        testScheduler.advanceUntilIdle()
        assertEquals(1, store.state.value.tracks.size)
    }
}
