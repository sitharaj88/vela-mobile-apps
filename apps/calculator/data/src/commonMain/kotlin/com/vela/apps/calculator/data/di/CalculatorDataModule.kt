/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.data.di

import com.vela.apps.calculator.data.InMemoryHistoryRepository
import com.vela.apps.calculator.domain.repository.HistoryRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Binds the calculator's data sources. Swap the repository impl here to move to Room. */
val calculatorDataModule = module {
    singleOf(::InMemoryHistoryRepository) bind HistoryRepository::class
}
