/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.presentation

import app.cash.turbine.test
import com.vela.apps.gallery.domain.ALL_MEDIA_ALBUM
import com.vela.apps.gallery.domain.FAVORITES_ALBUM
import com.vela.apps.gallery.domain.FavoritesRepository
import com.vela.apps.gallery.domain.MediaItem
import com.vela.apps.gallery.domain.MediaKind
import com.vela.apps.gallery.domain.MediaSort
import com.vela.apps.gallery.domain.MediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class GalleryStoreTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeMediaSource(var items: List<MediaItem>) : MediaSource {
        var thrown: Throwable? = null
        override suspend fun allItems(): List<MediaItem> = thrown?.let { throw it } ?: items
    }

    private class FakeFavoritesRepository : FavoritesRepository {
        val ids = MutableStateFlow<Set<String>>(emptySet())
        override fun observeFavorites(): Flow<Set<String>> = ids
        override suspend fun toggle(id: String) {
            ids.value = if (id in ids.value) ids.value - id else ids.value + id
        }
    }

    private fun item(id: String, name: String, folder: String, date: Long) = MediaItem(
        id = id,
        uri = "file:///$folder/$name",
        name = name,
        kind = MediaKind.Image,
        sizeBytes = 0,
        dateModifiedMs = date,
        folder = folder,
    )

    private val sample = listOf(
        item("1", "beach.jpg", "Camera", 30),
        item("2", "cat.png", "Camera", 10),
        item("3", "Screenshot.png", "Screenshots", 20),
    )

    private lateinit var source: FakeMediaSource
    private lateinit var favorites: FakeFavoritesRepository
    private lateinit var store: GalleryStore

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        source = FakeMediaSource(sample)
        favorites = FakeFavoritesRepository()
        store = GalleryStore(source, favorites)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun load_populates_albums_and_clears_loading() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        val state = store.state.value
        assertEquals(false, state.isLoading)
        assertEquals(3, state.allItems.size)
        assertEquals(listOf("Camera", "Screenshots"), state.albums.map { it.name })
    }

    @Test
    fun opening_album_applies_default_date_desc_sort() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(GalleryIntent.OpenAlbum("Camera"))

        val visible = store.state.value.visibleItems
        assertEquals(listOf("1", "2"), visible.map { it.id })
    }

    @Test
    fun all_media_album_shows_everything_sorted() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(GalleryIntent.OpenAlbum(ALL_MEDIA_ALBUM))

        assertEquals(listOf("1", "3", "2"), store.state.value.visibleItems.map { it.id })
    }

    @Test
    fun query_filters_visible_items_by_name() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(GalleryIntent.OpenAlbum(ALL_MEDIA_ALBUM))
        store.onIntent(GalleryIntent.SetQuery("cat"))

        assertEquals(listOf("2"), store.state.value.visibleItems.map { it.id })
    }

    @Test
    fun set_sort_reorders_visible_items() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(GalleryIntent.OpenAlbum("Camera"))
        store.onIntent(GalleryIntent.SetSort(MediaSort.NameAsc))

        assertEquals(listOf("beach.jpg", "cat.png"), store.state.value.visibleItems.map { it.name })
    }

    @Test
    fun toggle_view_mode_switches_grid_and_list() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        assertEquals(AlbumViewMode.Grid, store.state.value.viewMode)
        store.onIntent(GalleryIntent.ToggleViewMode)
        assertEquals(AlbumViewMode.List, store.state.value.viewMode)
    }

    @Test
    fun toggle_favorite_persists_and_reflects_in_favorites_album() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(GalleryIntent.ToggleFavorite("1"))
        testScheduler.advanceUntilIdle()
        store.onIntent(GalleryIntent.OpenAlbum(FAVORITES_ALBUM))

        assertTrue("1" in store.state.value.favorites)
        assertEquals(listOf("1"), store.state.value.visibleItems.map { it.id })
    }

    @Test
    fun load_failure_sets_error_and_emits_effect() = runTest(dispatcher) {
        source.thrown = IllegalStateException("boom")
        val failing = GalleryStore(source, favorites)
        failing.effects.test {
            failing.onIntent(GalleryIntent.Refresh)
            testScheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is GalleryEffect.ShowError)
            assertEquals("boom", failing.state.value.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
