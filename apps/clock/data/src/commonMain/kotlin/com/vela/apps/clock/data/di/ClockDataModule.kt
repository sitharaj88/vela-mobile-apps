/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.data.di

import com.vela.apps.clock.data.RoomAlarmRepository
import com.vela.apps.clock.data.local.AlarmsDatabase
import com.vela.apps.clock.data.local.alarmsPlatformDatabaseModule
import com.vela.apps.clock.data.local.buildAlarmsDatabase
import com.vela.apps.clock.domain.AlarmRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Wires the Clock alarms Room database, DAO, and repository binding. */
val clockDataModule = module {
    includes(alarmsPlatformDatabaseModule())
    single { buildAlarmsDatabase(get()) }
    single { get<AlarmsDatabase>().alarmDao() }
    singleOf(::RoomAlarmRepository) bind AlarmRepository::class
}
