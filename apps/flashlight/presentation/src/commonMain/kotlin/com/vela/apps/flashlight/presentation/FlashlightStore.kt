/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.presentation

import androidx.lifecycle.viewModelScope
import com.vela.apps.flashlight.domain.SosPattern
import com.vela.apps.flashlight.domain.Torch
import com.vela.core.common.MviStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** The light source / behaviour the user has selected. */
enum class FlashlightMode { Off, Torch, Strobe, Sos, ScreenLight }

/** Tint presets for the on-screen light — a pure-Compose feature that works on every platform. */
enum class ScreenTint { White, Warm, Cool, Red }

/**
 * Single screen state for the Flashlight app.
 *
 * @param mode the active [FlashlightMode].
 * @param torchAvailable whether this device exposes a controllable hardware flash.
 * @param strobeHz blink frequency for [FlashlightMode.Strobe] (full on/off cycles per second).
 * @param screenTint colour preset used by [FlashlightMode.ScreenLight].
 * @param brightness 0..1 brightness for the on-screen light.
 * @param emitterOn the current instantaneous on/off state of the light (drives torch + screen flash);
 *   for [FlashlightMode.Strobe] and [FlashlightMode.Sos] this toggles on the ticker.
 */
data class FlashlightState(
    val mode: FlashlightMode = FlashlightMode.Off,
    val torchAvailable: Boolean = false,
    val strobeHz: Float = DEFAULT_STROBE_HZ,
    val screenTint: ScreenTint = ScreenTint.White,
    val brightness: Float = DEFAULT_BRIGHTNESS,
    val emitterOn: Boolean = false,
) {
    /** True while the full-screen light surface should be shown. */
    val screenLightActive: Boolean
        get() = when (mode) {
            FlashlightMode.ScreenLight -> true
            FlashlightMode.Strobe, FlashlightMode.Sos -> !torchAvailable
            FlashlightMode.Off, FlashlightMode.Torch -> false
        }

    companion object {
        const val DEFAULT_STROBE_HZ: Float = 4f
        const val MIN_STROBE_HZ: Float = 0.5f
        const val MAX_STROBE_HZ: Float = 20f
        const val DEFAULT_BRIGHTNESS: Float = 1f
    }
}

/** User actions. */
sealed interface FlashlightIntent {
    /** Toggle the big power button: on -> last useful mode, off -> [FlashlightMode.Off]. */
    data object TogglePower : FlashlightIntent

    /** Switch to an explicit [FlashlightMode] (segmented selector). */
    data class SelectMode(val mode: FlashlightMode) : FlashlightIntent

    /** Update the strobe frequency in Hz. */
    data class SetStrobeHz(val hz: Float) : FlashlightIntent

    /** Update the on-screen light tint. */
    data class SetTint(val tint: ScreenTint) : FlashlightIntent

    /** Update the on-screen light brightness (0..1). */
    data class SetBrightness(val value: Float) : FlashlightIntent
}

/**
 * Drives the device [Torch] and the on-screen light across Off / Torch / Strobe / SOS / Screen-Light
 * modes. Strobe and SOS run on [viewModelScope] coroutine tickers that toggle [FlashlightState.emitterOn]
 * (and the hardware torch when available). No one-shot effects, so the effect type is [Nothing].
 */
class FlashlightStore(
    private val torch: Torch,
) : MviStore<FlashlightState, FlashlightIntent, Nothing>(
    FlashlightState(torchAvailable = torch.isAvailable),
) {

    private var blinkJob: Job? = null

    override fun onIntent(intent: FlashlightIntent) {
        when (intent) {
            FlashlightIntent.TogglePower -> togglePower()
            is FlashlightIntent.SelectMode -> selectMode(intent.mode)
            is FlashlightIntent.SetStrobeHz -> setStrobeHz(intent.hz)
            is FlashlightIntent.SetTint -> setState { copy(screenTint = intent.tint) }
            is FlashlightIntent.SetBrightness ->
                setState { copy(brightness = intent.value.coerceIn(0f, 1f)) }
        }
    }

    private fun togglePower() {
        val target = if (currentState.mode == FlashlightMode.Off) {
            if (currentState.torchAvailable) FlashlightMode.Torch else FlashlightMode.ScreenLight
        } else {
            FlashlightMode.Off
        }
        selectMode(target)
    }

    private fun setStrobeHz(hz: Float) {
        val clamped = hz.coerceIn(FlashlightState.MIN_STROBE_HZ, FlashlightState.MAX_STROBE_HZ)
        setState { copy(strobeHz = clamped) }
        if (currentState.mode == FlashlightMode.Strobe) {
            // Restart the ticker so the new frequency takes effect immediately.
            startMode(FlashlightMode.Strobe)
        }
    }

    private fun selectMode(mode: FlashlightMode) {
        if (mode == FlashlightMode.Torch && !currentState.torchAvailable) return
        startMode(mode)
    }

    private fun startMode(mode: FlashlightMode) {
        blinkJob?.cancel()
        blinkJob = null
        when (mode) {
            FlashlightMode.Off -> {
                setEmitter(false)
                setState { copy(mode = FlashlightMode.Off, emitterOn = false) }
            }
            FlashlightMode.Torch -> {
                setState { copy(mode = FlashlightMode.Torch) }
                setEmitter(true)
                setState { copy(emitterOn = true) }
            }
            FlashlightMode.ScreenLight -> {
                // Screen light is rendered by Compose; the hardware torch stays off.
                setEmitter(false)
                setState { copy(mode = FlashlightMode.ScreenLight, emitterOn = true) }
            }
            FlashlightMode.Strobe -> {
                setState { copy(mode = FlashlightMode.Strobe, emitterOn = false) }
                blinkJob = viewModelScope.launch { runStrobe() }
            }
            FlashlightMode.Sos -> {
                setState { copy(mode = FlashlightMode.Sos, emitterOn = false) }
                blinkJob = viewModelScope.launch { runSos() }
            }
        }
    }

    private suspend fun runStrobe() {
        while (viewModelScope.isActive) {
            val hz = currentState.strobeHz.coerceAtLeast(FlashlightState.MIN_STROBE_HZ)
            val halfPeriod = (MILLIS_PER_SECOND / (hz * 2f)).toLong().coerceAtLeast(1L)
            pulse(on = true, durationMillis = halfPeriod)
            pulse(on = false, durationMillis = halfPeriod)
        }
    }

    private suspend fun runSos() {
        while (viewModelScope.isActive) {
            for (step in SosPattern.steps()) {
                pulse(on = step.on, durationMillis = step.durationMillis)
            }
        }
    }

    /** Set the emitter to [on], reflect it in state, then wait [durationMillis]. */
    private suspend fun pulse(on: Boolean, durationMillis: Long) {
        setEmitter(on)
        setState { copy(emitterOn = on) }
        delay(durationMillis)
    }

    /** Push an on/off command to the hardware torch (no-op when unavailable). */
    private fun setEmitter(on: Boolean) {
        if (currentState.torchAvailable) torch.setEnabled(on)
    }

    override fun onCleared() {
        blinkJob?.cancel()
        if (currentState.torchAvailable) torch.setEnabled(false)
        super.onCleared()
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000f
    }
}
