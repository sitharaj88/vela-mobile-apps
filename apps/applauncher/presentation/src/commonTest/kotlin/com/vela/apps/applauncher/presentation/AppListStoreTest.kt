/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.presentation

import app.cash.turbine.test
import com.vela.apps.applauncher.domain.AppEntry
import com.vela.apps.applauncher.domain.FavoritesRepository
import com.vela.apps.applauncher.domain.InstalledApps
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppListStoreTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeInstalledApps(
        private val apps: List<AppEntry>,
        override val isSupported: Boolean = true,
    ) : InstalledApps {
        val launched = mutableListOf<String>()
        override suspend fun list(): List<AppEntry> = apps
        override fun launch(id: String) { launched += id }
    }

    private class FakeFavoritesRepository : FavoritesRepository {
        val ids = MutableStateFlow<Set<String>>(emptySet())
        override fun observeFavorites(): Flow<Set<String>> = ids
        override suspend fun toggle(id: String) {
            ids.value = if (id in ids.value) ids.value - id else ids.value + id
        }
    }

    private val sampleApps = listOf(
        AppEntry(id = "pkg.zen", label = "Zen"),
        AppEntry(id = "pkg.browser", label = "Browser"),
        AppEntry(id = "pkg.calc", label = "Calculator"),
    )

    private lateinit var favorites: FakeFavoritesRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        favorites = FakeFavoritesRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun store(
        apps: List<AppEntry> = sampleApps,
        supported: Boolean = true,
    ) = AppListStore(FakeInstalledApps(apps, supported), favorites)

    @Test
    fun loads_apps_sorted_alphabetically_and_clears_loading() = runTest(dispatcher) {
        val store = store()
        testScheduler.advanceUntilIdle()

        val state = store.state.value
        assertEquals(listOf("Browser", "Calculator", "Zen"), state.all.map { it.label })
        assertEquals(state.all, state.filtered)
        assertFalse(state.loading)
        assertTrue(state.supported)
    }

    @Test
    fun search_filters_by_label_case_insensitively() = runTest(dispatcher) {
        val store = store()
        testScheduler.advanceUntilIdle()

        store.onIntent(AppListIntent.Search("br"))
        assertEquals(listOf("Browser"), store.state.value.filtered.map { it.label })
        assertEquals("br", store.state.value.query)
    }

    @Test
    fun toggling_favorite_persists_and_updates_favorites_section() = runTest(dispatcher) {
        val store = store()
        testScheduler.advanceUntilIdle()

        store.onIntent(AppListIntent.ToggleFavorite("pkg.calc"))
        testScheduler.advanceUntilIdle()

        assertEquals(setOf("pkg.calc"), store.state.value.favoriteIds)
        assertEquals(listOf("Calculator"), store.state.value.favorites.map { it.label })

        store.onIntent(AppListIntent.ToggleFavorite("pkg.calc"))
        testScheduler.advanceUntilIdle()
        assertTrue(store.state.value.favoriteIds.isEmpty())
        assertTrue(store.state.value.favorites.isEmpty())
    }

    @Test
    fun launch_delegates_to_platform_and_emits_effect() = runTest(dispatcher) {
        val installed = FakeInstalledApps(sampleApps)
        val store = AppListStore(installed, favorites)
        testScheduler.advanceUntilIdle()

        store.effects.test {
            store.onIntent(AppListIntent.Launch("pkg.zen"))
            val effect = awaitItem()
            assertTrue(effect is AppListEffect.Launched)
            assertEquals("Zen", (effect as AppListEffect.Launched).label)
        }
        assertEquals(listOf("pkg.zen"), installed.launched)
    }

    @Test
    fun unsupported_platform_reports_no_apps_and_not_supported() = runTest(dispatcher) {
        val store = store(apps = emptyList(), supported = false)
        testScheduler.advanceUntilIdle()

        assertFalse(store.state.value.supported)
        assertTrue(store.state.value.all.isEmpty())
        assertFalse(store.state.value.loading)
    }
}
