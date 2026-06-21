/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vela.apps.calendar.ui.screen.CalendarScreen
import com.vela.apps.calendar.ui.screen.EventEditorScreen
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import kotlinx.serialization.Serializable

/** Type-safe navigation routes for the Calendar app. */
object CalendarRoutes {
    @Serializable
    object Month

    @Serializable
    data class Editor(val eventId: Long, val epochDay: Long)
}

/**
 * Root composable for Calendar. Platforms call this after starting Koin with
 * [com.vela.apps.calendar.ui.di.calendarModule].
 */
@Composable
fun CalendarApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Indigo, themeMode = themeMode, dynamicColor = true) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = CalendarRoutes.Month) {
            composable<CalendarRoutes.Month> {
                CalendarScreen(
                    onOpenEditor = { eventId, epochDay ->
                        navController.navigate(CalendarRoutes.Editor(eventId, epochDay))
                    },
                )
            }
            composable<CalendarRoutes.Editor> { backStackEntry ->
                val route = backStackEntry.toRoute<CalendarRoutes.Editor>()
                EventEditorScreen(
                    eventId = route.eventId,
                    epochDay = route.epochDay,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
