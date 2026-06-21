/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.presentation.timer

import androidx.lifecycle.viewModelScope
import com.vela.apps.clock.domain.formatTimer
import com.vela.core.common.MviStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration

data class TimerState(
    val display: String = formatTimer(Duration.ZERO),
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val hasDuration: Boolean = false,
)

sealed interface TimerIntent {
    data class SetDuration(val duration: Duration) : TimerIntent
    data object ToggleRunning : TimerIntent
    data object Reset : TimerIntent
}

sealed interface TimerEffect {
    data object Finished : TimerEffect
}

/** Countdown timer driven by a monotonic time source; emits [TimerEffect.Finished] at zero. */
class TimerStore : MviStore<TimerState, TimerIntent, TimerEffect>(TimerState()) {

    private var configured: Duration = Duration.ZERO
    private var remaining: Duration = Duration.ZERO
    private var startMark: kotlin.time.TimeMark? = null
    private var ticker: Job? = null

    private fun currentRemaining(): Duration =
        (remaining - (startMark?.elapsedNow() ?: Duration.ZERO)).coerceAtLeast(Duration.ZERO)

    override fun onIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.SetDuration -> setDuration(intent.duration)
            TimerIntent.ToggleRunning -> if (startMark == null) start() else pause()
            TimerIntent.Reset -> reset()
        }
    }

    private fun setDuration(duration: Duration) {
        ticker?.cancel()
        configured = duration
        remaining = duration
        startMark = null
        setState {
            copy(
                display = formatTimer(duration),
                isRunning = false,
                isFinished = false,
                hasDuration = duration > Duration.ZERO,
            )
        }
    }

    private fun start() {
        if (remaining <= Duration.ZERO) return
        startMark = kotlin.time.TimeSource.Monotonic.markNow()
        setState { copy(isRunning = true, isFinished = false) }
        ticker = viewModelScope.launch {
            while (isActive) {
                val left = currentRemaining()
                setState { copy(display = formatTimer(left)) }
                if (left <= Duration.ZERO) {
                    finish()
                    break
                }
                delay(REFRESH_MS)
            }
        }
    }

    private fun pause() {
        remaining = currentRemaining()
        startMark = null
        ticker?.cancel()
        setState { copy(isRunning = false) }
    }

    private fun reset() {
        ticker?.cancel()
        remaining = configured
        startMark = null
        setState { copy(display = formatTimer(configured), isRunning = false, isFinished = false) }
    }

    private fun finish() {
        remaining = Duration.ZERO
        startMark = null
        setState { copy(isRunning = false, isFinished = true, display = formatTimer(Duration.ZERO)) }
        emitEffect(TimerEffect.Finished)
    }

    private companion object {
        const val REFRESH_MS = 100L
    }
}
