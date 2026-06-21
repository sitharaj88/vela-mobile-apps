/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.presentation.stopwatch

import androidx.lifecycle.viewModelScope
import com.vela.apps.clock.domain.Lap
import com.vela.apps.clock.domain.formatStopwatch
import com.vela.core.common.MviStore
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

data class StopwatchState(
    val isRunning: Boolean = false,
    val display: String = formatStopwatch(Duration.ZERO),
    val laps: PersistentList<Lap> = persistentListOf(),
)

sealed interface StopwatchIntent {
    data object ToggleRunning : StopwatchIntent
    data object Reset : StopwatchIntent
    data object Lap : StopwatchIntent
}

/**
 * Stopwatch driven by a monotonic time source (drift-free; the ticking coroutine only refreshes the
 * displayed value, it does not accumulate the time itself).
 */
class StopwatchStore : MviStore<StopwatchState, StopwatchIntent, Nothing>(StopwatchState()) {

    private var accumulated: Duration = Duration.ZERO
    private var startMark: TimeMark? = null
    private var lastLapElapsed: Duration = Duration.ZERO
    private var ticker: Job? = null

    private fun elapsed(): Duration = accumulated + (startMark?.elapsedNow() ?: Duration.ZERO)

    override fun onIntent(intent: StopwatchIntent) {
        when (intent) {
            StopwatchIntent.ToggleRunning -> if (startMark == null) start() else pause()
            StopwatchIntent.Reset -> reset()
            StopwatchIntent.Lap -> recordLap()
        }
    }

    private fun start() {
        startMark = TimeSource.Monotonic.markNow()
        setState { copy(isRunning = true) }
        ticker = viewModelScope.launch {
            while (isActive) {
                setState { copy(display = formatStopwatch(elapsed())) }
                delay(REFRESH_MS)
            }
        }
    }

    private fun pause() {
        accumulated = elapsed()
        startMark = null
        ticker?.cancel()
        setState { copy(isRunning = false, display = formatStopwatch(accumulated)) }
    }

    private fun reset() {
        ticker?.cancel()
        accumulated = Duration.ZERO
        startMark = null
        lastLapElapsed = Duration.ZERO
        setState { StopwatchState() }
    }

    private fun recordLap() {
        val total = elapsed()
        val split = total - lastLapElapsed
        lastLapElapsed = total
        setState {
            copy(laps = laps.add(0, Lap(laps.size + 1, formatStopwatch(split), formatStopwatch(total))))
        }
    }

    private companion object {
        const val REFRESH_MS = 31L
    }
}
