/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.ui.di

import com.vela.apps.calculator.data.di.calculatorDataModule
import com.vela.apps.calculator.presentation.CalculatorStore
import com.vela.apps.calculator.presentation.di.calculatorPresentationModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole Calculator app. */
val calculatorModule = module {
    includes(calculatorDataModule, calculatorPresentationModule)
    viewModelOf(::CalculatorStore)
}
