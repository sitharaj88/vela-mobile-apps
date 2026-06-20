/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.presentation.di

import com.vela.apps.calculator.domain.engine.ExpressionEvaluator
import com.vela.apps.calculator.domain.usecase.EvaluateExpressionUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Use cases for the calculator. The [com.vela.apps.calculator.presentation.CalculatorStore] itself
 * is bound in the UI module, where the Compose-aware ViewModel DSL lives — keeping this layer free
 * of any Compose dependency.
 */
val calculatorPresentationModule = module {
    factoryOf(::ExpressionEvaluator)
    factoryOf(::EvaluateExpressionUseCase)
}
