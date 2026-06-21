/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.di

import com.vela.apps.clock.domain.AlarmRepository
import com.vela.apps.clock.domain.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Starts Koin with the Clock modules. Each platform entry point calls this once at launch. */
fun initClockKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(clockModule)
}

/** No-arg convenience for Swift interop. */
fun doInitClockKoin() {
    initClockKoin()
}

/**
 * Re-arms every enabled alarm with the platform scheduler. Call once after [initClockKoin] so alarms
 * created in a previous session keep firing (essential for the in-process Desktop scheduler).
 */
fun KoinApplication.rescheduleAlarms() {
    val repository = koin.get<AlarmRepository>()
    val scheduler = koin.get<AlarmScheduler>()
    CoroutineScope(SupervisorJob()).launch {
        repository.observeAlarms().first()
            .filter { it.enabled }
            .forEach(scheduler::schedule)
    }
}
