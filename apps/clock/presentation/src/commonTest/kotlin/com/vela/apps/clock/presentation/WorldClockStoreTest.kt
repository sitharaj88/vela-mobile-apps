/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.presentation

import com.vela.apps.clock.presentation.world.WorldClockIntent
import com.vela.apps.clock.presentation.world.WorldClockStore
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
class WorldClockStoreTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var store: WorldClockStore

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        store = WorldClockStore()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun starts_with_default_zones_rendered() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        // The first tick populates rows for the default zones.
        assertTrue(store.state.value.rows.isNotEmpty())
    }

    @Test
    fun picker_excludes_already_pinned_zones() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        val pinned = store.state.value.rows.map { it.zoneId }.toSet()
        val available = store.state.value.available.map { it.id }.toSet()
        assertTrue(pinned.none { it in available })
    }

    @Test
    fun adding_a_zone_appends_a_row() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        val before = store.state.value.rows.size
        store.onIntent(WorldClockIntent.AddZone("Australia/Sydney"))
        testScheduler.advanceUntilIdle()
        assertEquals(before + 1, store.state.value.rows.size)
        assertTrue(store.state.value.rows.any { it.zoneId == "Australia/Sydney" })
    }

    @Test
    fun removing_a_zone_drops_its_row() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        val target = store.state.value.rows.first().zoneId
        store.onIntent(WorldClockIntent.RemoveZone(target))
        testScheduler.advanceUntilIdle()
        assertTrue(store.state.value.rows.none { it.zoneId == target })
    }
}
