/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.presentation.world

import androidx.lifecycle.viewModelScope
import com.vela.apps.clock.domain.WorldClockDisplay
import com.vela.apps.clock.domain.cityNameOf
import com.vela.apps.clock.domain.formatWorldClock
import com.vela.core.common.MviStore
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

data class PickableZone(val id: String, val cityName: String)

data class WorldClockState(
    val rows: PersistentList<WorldClockDisplay> = persistentListOf(),
    val picking: Boolean = false,
    val available: PersistentList<PickableZone> = persistentListOf(),
)

sealed interface WorldClockIntent {
    data object OpenPicker : WorldClockIntent
    data object ClosePicker : WorldClockIntent
    data class AddZone(val zoneId: String) : WorldClockIntent
    data class RemoveZone(val zoneId: String) : WorldClockIntent
}

/**
 * Shows the current time across a set of pinned timezones, refreshing once per second. The picker
 * lists every [TimeZone.availableZoneIds] not already pinned.
 */
class WorldClockStore : MviStore<WorldClockState, WorldClockIntent, Nothing>(WorldClockState()) {

    private var zoneIds: List<String> = DEFAULT_ZONES.filter { it in TimeZone.availableZoneIds }

    init {
        rebuildAvailable()
        viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(TICK_MS)
            }
        }
    }

    override fun onIntent(intent: WorldClockIntent) {
        when (intent) {
            WorldClockIntent.OpenPicker -> setState { copy(picking = true) }
            WorldClockIntent.ClosePicker -> setState { copy(picking = false) }
            is WorldClockIntent.AddZone -> addZone(intent.zoneId)
            is WorldClockIntent.RemoveZone -> removeZone(intent.zoneId)
        }
    }

    private fun addZone(zoneId: String) {
        if (zoneId in zoneIds || zoneId !in TimeZone.availableZoneIds) return
        zoneIds = zoneIds + zoneId
        rebuildAvailable()
        refresh()
        setState { copy(picking = false) }
    }

    private fun removeZone(zoneId: String) {
        zoneIds = zoneIds - zoneId
        rebuildAvailable()
        refresh()
    }

    private fun rebuildAvailable() {
        val pinned = zoneIds.toSet()
        val available = TimeZone.availableZoneIds
            .asSequence()
            .filter { it !in pinned && '/' in it }
            .sorted()
            .map { PickableZone(it, cityNameOf(it)) }
            .toList()
            .toPersistentList()
        setState { copy(available = available) }
    }

    private fun refresh() {
        val now = Clock.System.now()
        val device = TimeZone.currentSystemDefault()
        val rows = zoneIds
            .map { formatWorldClock(now, TimeZone.of(it), device) }
            .toPersistentList()
        setState { copy(rows = rows) }
    }

    private companion object {
        const val TICK_MS = 1000L
        val DEFAULT_ZONES = listOf(
            "America/Los_Angeles",
            "America/New_York",
            "Europe/London",
            "Asia/Kolkata",
            "Asia/Tokyo",
        )
    }
}
