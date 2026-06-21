/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.di

import com.vela.apps.clock.data.di.clockDataModule
import com.vela.apps.clock.presentation.alarms.AlarmsStore
import com.vela.apps.clock.presentation.clock.ClockStore
import com.vela.apps.clock.presentation.stopwatch.StopwatchStore
import com.vela.apps.clock.presentation.timer.TimerStore
import com.vela.apps.clock.presentation.world.WorldClockStore
import com.vela.apps.clock.ui.platform.clockPlatformModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole Clock app. */
val clockModule = module {
    includes(clockDataModule, clockPlatformModule())
    viewModelOf(::ClockStore)
    viewModelOf(::StopwatchStore)
    viewModelOf(::TimerStore)
    viewModelOf(::AlarmsStore)
    viewModelOf(::WorldClockStore)
}
