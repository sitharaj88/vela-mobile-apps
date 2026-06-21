/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.clock.presentation.timer.TimerIntent
import com.vela.apps.clock.presentation.timer.TimerStore
import com.vela.core.designsystem.component.VelaButton
import com.vela.core.designsystem.component.VelaButtonVariant
import com.vela.core.designsystem.theme.LocalVelaTokens
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val presets: List<Pair<String, Duration>> = listOf(
    "1 min" to 1.minutes,
    "3 min" to 3.minutes,
    "5 min" to 5.minutes,
    "10 min" to 10.minutes,
)

@Composable
fun TimerTab(store: TimerStore) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current

    Column(
        modifier = Modifier.fillMaxSize().padding(tokens.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
            presets.forEach { (label, duration) ->
                AssistChip(
                    onClick = { store.onIntent(TimerIntent.SetDuration(duration)) },
                    label = { Text(label) },
                )
            }
        }

        Text(
            text = state.display,
            style = MaterialTheme.typography.displayLarge,
            color = if (state.isFinished) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = tokens.spacing.xxl),
        )

        if (state.isFinished) {
            Text(
                "Time's up!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = tokens.spacing.lg),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            VelaButton(
                text = "Reset",
                onClick = { store.onIntent(TimerIntent.Reset) },
                variant = VelaButtonVariant.Tonal,
                modifier = Modifier.weight(1f),
            )
            VelaButton(
                text = if (state.isRunning) "Pause" else "Start",
                onClick = { store.onIntent(TimerIntent.ToggleRunning) },
                enabled = state.hasDuration,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
