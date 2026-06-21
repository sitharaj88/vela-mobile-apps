/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.presentation.clock

import androidx.lifecycle.viewModelScope
import com.vela.apps.clock.domain.formatClock
import com.vela.core.common.MviStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

data class ClockState(val time: String = "", val date: String = "")

/** Live wall-clock that re-reads the system clock once per second. No user intents. */
class ClockStore : MviStore<ClockState, Nothing, Nothing>(ClockState()) {

    init {
        viewModelScope.launch {
            while (isActive) {
                val display = formatClock(Clock.System.now(), TimeZone.currentSystemDefault())
                setState { copy(time = display.time, date = display.date) }
                delay(TICK_MS)
            }
        }
    }

    override fun onIntent(intent: Nothing) = Unit

    private companion object {
        const val TICK_MS = 1000L
    }
}
