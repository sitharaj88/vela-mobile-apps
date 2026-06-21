/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.vela.apps.clock.presentation.alarms.AlarmsStore
import com.vela.apps.clock.presentation.clock.ClockStore
import com.vela.apps.clock.presentation.stopwatch.StopwatchStore
import com.vela.apps.clock.presentation.timer.TimerStore
import com.vela.apps.clock.presentation.world.WorldClockStore
import com.vela.apps.clock.ui.tab.AlarmsTab
import com.vela.apps.clock.ui.tab.ClockTab
import com.vela.apps.clock.ui.tab.StopwatchTab
import com.vela.apps.clock.ui.tab.TimerTab
import com.vela.apps.clock.ui.tab.WorldClockTab
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import org.koin.compose.viewmodel.koinViewModel

private enum class ClockTabId(val label: String, val icon: ImageVector) {
    Clock("Clock", Icons.Filled.Schedule),
    Alarms("Alarms", Icons.Filled.Alarm),
    Stopwatch("Stopwatch", Icons.Filled.Timer),
    Timer("Timer", Icons.Filled.HourglassEmpty),
    World("World", Icons.Filled.Public),
}

/**
 * Root composable for Clock. All stores are hoisted here (shared across tab switches) so the
 * stopwatch/timer/world clock keep running while you look at another tab.
 */
@Composable
fun ClockApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Forest, themeMode = themeMode, dynamicColor = true) {
        val clockStore: ClockStore = koinViewModel()
        val alarmsStore: AlarmsStore = koinViewModel()
        val stopwatchStore: StopwatchStore = koinViewModel()
        val timerStore: TimerStore = koinViewModel()
        val worldStore: WorldClockStore = koinViewModel()
        var selected by remember { mutableStateOf(ClockTabId.Clock) }

        VelaScaffold(title = "Vela Clock") { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Box(Modifier.weight(1f).fillMaxSize()) {
                    when (selected) {
                        ClockTabId.Clock -> ClockTab(clockStore)
                        ClockTabId.Alarms -> AlarmsTab(alarmsStore)
                        ClockTabId.Stopwatch -> StopwatchTab(stopwatchStore)
                        ClockTabId.Timer -> TimerTab(timerStore)
                        ClockTabId.World -> WorldClockTab(worldStore)
                    }
                }
                NavigationBar {
                    ClockTabId.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selected == tab,
                            onClick = { selected = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        }
    }
}
