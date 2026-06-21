/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.clock.presentation.stopwatch.StopwatchIntent
import com.vela.apps.clock.presentation.stopwatch.StopwatchStore
import com.vela.core.designsystem.component.VelaButton
import com.vela.core.designsystem.component.VelaButtonVariant
import com.vela.core.designsystem.theme.LocalVelaTokens

@Composable
fun StopwatchTab(store: StopwatchStore) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current

    Column(modifier = Modifier.fillMaxSize().padding(tokens.spacing.xl)) {
        Text(
            text = state.display,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.fillMaxWidth().padding(vertical = tokens.spacing.xl),
            textAlign = TextAlign.Center,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            VelaButton(
                text = if (state.isRunning) "Lap" else "Reset",
                onClick = {
                    store.onIntent(if (state.isRunning) StopwatchIntent.Lap else StopwatchIntent.Reset)
                },
                variant = VelaButtonVariant.Tonal,
                modifier = Modifier.weight(1f),
            )
            VelaButton(
                text = if (state.isRunning) "Pause" else "Start",
                onClick = { store.onIntent(StopwatchIntent.ToggleRunning) },
                modifier = Modifier.weight(1f),
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = tokens.spacing.lg)) {
            items(state.laps, key = { it.index }) { lap ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = tokens.spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Lap ${lap.index}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        lap.split,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(lap.total, style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()
            }
        }
    }
}
