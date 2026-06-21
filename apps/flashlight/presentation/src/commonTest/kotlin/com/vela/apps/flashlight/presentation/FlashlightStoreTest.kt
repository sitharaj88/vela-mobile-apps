/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.presentation

import app.cash.turbine.test
import com.vela.apps.flashlight.domain.Torch
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FlashlightStoreTest {

    private val dispatcher = StandardTestDispatcher()

    /** Records every on/off command pushed to the hardware torch. */
    private class FakeTorch(override val isAvailable: Boolean) : Torch {
        val commands = mutableListOf<Boolean>()
        var enabled = false
            private set

        override fun setEnabled(on: Boolean) {
            enabled = on
            commands += on
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun store(available: Boolean = true): Pair<FlashlightStore, FakeTorch> {
        val torch = FakeTorch(available)
        return FlashlightStore(torch) to torch
    }

    @Test
    fun initial_state_reflects_torch_availability() {
        val (s, _) = store(available = true)
        assertEquals(FlashlightMode.Off, s.state.value.mode)
        assertTrue(s.state.value.torchAvailable)
        assertFalse(s.state.value.emitterOn)
    }

    @Test
    fun toggle_power_turns_torch_on_then_off() = runTest(dispatcher) {
        val (s, torch) = store(available = true)

        s.onIntent(FlashlightIntent.TogglePower)
        assertEquals(FlashlightMode.Torch, s.state.value.mode)
        assertTrue(s.state.value.emitterOn)
        assertTrue(torch.enabled)

        s.onIntent(FlashlightIntent.TogglePower)
        assertEquals(FlashlightMode.Off, s.state.value.mode)
        assertFalse(s.state.value.emitterOn)
        assertFalse(torch.enabled)
    }

    @Test
    fun toggle_power_without_torch_falls_back_to_screen_light() {
        val (s, torch) = store(available = false)

        s.onIntent(FlashlightIntent.TogglePower)

        assertEquals(FlashlightMode.ScreenLight, s.state.value.mode)
        assertTrue(s.state.value.screenLightActive)
        assertTrue(torch.commands.none { it }, "screen light must not drive the hardware torch")
    }

    @Test
    fun selecting_torch_when_unavailable_is_ignored() {
        val (s, _) = store(available = false)
        s.onIntent(FlashlightIntent.SelectMode(FlashlightMode.Torch))
        assertEquals(FlashlightMode.Off, s.state.value.mode)
    }

    @Test
    fun screen_light_active_for_blink_modes_when_no_torch() {
        val (s, _) = store(available = false)
        s.onIntent(FlashlightIntent.SelectMode(FlashlightMode.Sos))
        assertTrue(s.state.value.screenLightActive)
    }

    @Test
    fun strobe_hz_is_clamped_to_range() {
        val (s, _) = store(available = true)

        s.onIntent(FlashlightIntent.SetStrobeHz(1000f))
        assertEquals(FlashlightState.MAX_STROBE_HZ, s.state.value.strobeHz)

        s.onIntent(FlashlightIntent.SetStrobeHz(0f))
        assertEquals(FlashlightState.MIN_STROBE_HZ, s.state.value.strobeHz)
    }

    @Test
    fun brightness_is_clamped_to_unit_range() {
        val (s, _) = store(available = true)
        s.onIntent(FlashlightIntent.SetBrightness(5f))
        assertEquals(1f, s.state.value.brightness)
        s.onIntent(FlashlightIntent.SetBrightness(-1f))
        assertEquals(0f, s.state.value.brightness)
    }

    @Test
    fun strobe_toggles_emitter_over_time() = runTest(dispatcher) {
        val (s, torch) = store(available = true)
        s.onIntent(FlashlightIntent.SetStrobeHz(5f))

        s.state.test {
            assertEquals(FlashlightMode.Off, awaitItem().mode) // initial

            s.onIntent(FlashlightIntent.SelectMode(FlashlightMode.Strobe))
            // mode -> Strobe with emitterOn=false
            assertEquals(FlashlightMode.Strobe, awaitItem().mode)

            // first pulse on
            assertTrue(awaitItem().emitterOn)
            // advance one half-period -> off
            testScheduler.advanceTimeBy(MS_5HZ_HALF)
            testScheduler.runCurrent()
            assertFalse(awaitItem().emitterOn)

            s.onIntent(FlashlightIntent.SelectMode(FlashlightMode.Off))
            assertEquals(FlashlightMode.Off, awaitItem().mode)
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(torch.commands.contains(true))
        assertFalse(torch.enabled, "torch must end off after switching to Off")
    }

    @Test
    fun sos_starts_with_an_on_pulse_and_drives_the_torch() = runTest(dispatcher) {
        val (s, torch) = store(available = true)

        s.onIntent(FlashlightIntent.SelectMode(FlashlightMode.Sos))
        testScheduler.runCurrent()

        assertEquals(FlashlightMode.Sos, s.state.value.mode)
        assertTrue(s.state.value.emitterOn, "SOS begins with the first dot lit")
        assertTrue(torch.commands.first(), "first hardware command is on")

        s.onIntent(FlashlightIntent.SelectMode(FlashlightMode.Off))
        testScheduler.runCurrent()
        assertFalse(torch.enabled)
    }

    private companion object {
        const val MS_5HZ_HALF = 100L // 1000 / (5 * 2)
    }
}
