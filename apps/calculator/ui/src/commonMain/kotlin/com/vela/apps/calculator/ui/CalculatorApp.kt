/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vela.apps.calculator.presentation.CalculatorStore
import com.vela.apps.calculator.ui.screen.CalculatorScreen
import com.vela.apps.calculator.ui.screen.HistoryScreen
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/** Type-safe navigation routes for the Calculator app. */
object CalculatorRoutes {
    @Serializable
    object Home

    @Serializable
    object History
}

/**
 * Root composable for the Calculator. Platforms (Android/iOS/Desktop) call this after starting
 * Koin with [com.vela.apps.calculator.ui.di.calculatorModule].
 *
 * The [CalculatorStore] is resolved once at the NavHost root and shared by both screens, so the
 * calculator and history views observe the same state (a per-destination ViewModel would not).
 */
@Composable
fun CalculatorApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Teal, themeMode = themeMode, dynamicColor = true) {
        val navController = rememberNavController()
        val store: CalculatorStore = koinViewModel()
        NavHost(navController = navController, startDestination = CalculatorRoutes.Home) {
            composable<CalculatorRoutes.Home> {
                CalculatorScreen(
                    store = store,
                    onOpenHistory = { navController.navigate(CalculatorRoutes.History) },
                )
            }
            composable<CalculatorRoutes.History> {
                HistoryScreen(store = store, onBack = { navController.popBackStack() })
            }
        }
    }
}
