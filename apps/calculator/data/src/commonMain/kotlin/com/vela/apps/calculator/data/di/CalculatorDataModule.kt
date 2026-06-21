/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.data.di

import com.vela.apps.calculator.data.RoomHistoryRepository
import com.vela.apps.calculator.data.local.CalculatorDatabase
import com.vela.apps.calculator.data.local.buildCalculatorDatabase
import com.vela.apps.calculator.data.local.platformDatabaseModule
import com.vela.apps.calculator.domain.repository.HistoryRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Wires the calculator's Room database, DAO, and the repository binding. */
val calculatorDataModule = module {
    includes(platformDatabaseModule())
    single { buildCalculatorDatabase(get()) }
    single { get<CalculatorDatabase>().historyDao() }
    singleOf(::RoomHistoryRepository) bind HistoryRepository::class
}
