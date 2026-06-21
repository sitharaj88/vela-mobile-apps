/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.clock.domain.Alarm
import com.vela.apps.clock.domain.WeekDay
import com.vela.apps.clock.domain.repeatSummary
import com.vela.apps.clock.domain.shortLabel
import com.vela.apps.clock.presentation.alarms.AlarmDraft
import com.vela.apps.clock.presentation.alarms.AlarmsIntent
import com.vela.apps.clock.presentation.alarms.AlarmsStore
import com.vela.core.designsystem.component.VelaButton
import com.vela.core.designsystem.component.VelaButtonVariant
import com.vela.core.designsystem.component.VelaCard
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.theme.LocalVelaTokens

private fun pad2(value: Int): String = value.toString().padStart(2, '0')

@Composable
fun AlarmsTab(store: AlarmsStore) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current

    Box(Modifier.fillMaxSize()) {
        if (state.alarms.isEmpty()) {
            VelaEmptyState(
                icon = Icons.Filled.Alarm,
                title = "No alarms yet",
                description = "Tap + to add your first alarm.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(tokens.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            ) {
                items(state.alarms, key = { it.id }) { alarm ->
                    AlarmRow(
                        alarm = alarm,
                        onToggle = { store.onIntent(AlarmsIntent.ToggleEnabled(alarm.id, it)) },
                        onEdit = { store.onIntent(AlarmsIntent.Edit(alarm.id)) },
                        onDelete = { store.onIntent(AlarmsIntent.Delete(alarm.id)) },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { store.onIntent(AlarmsIntent.AddNew) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(tokens.spacing.lg),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add alarm")
        }
    }

    state.editor?.let { draft ->
        AlarmEditorDialog(
            draft = draft,
            onTime = { h, m -> store.onIntent(AlarmsIntent.DraftTime(h, m)) },
            onLabel = { store.onIntent(AlarmsIntent.DraftLabel(it)) },
            onToggleDay = { store.onIntent(AlarmsIntent.ToggleDraftDay(it)) },
            onSave = { store.onIntent(AlarmsIntent.SaveDraft) },
            onDismiss = { store.onIntent(AlarmsIntent.DismissEditor) },
        )
    }
}

@Composable
private fun AlarmRow(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val tokens = LocalVelaTokens.current
    VelaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${pad2(alarm.hour)}:${pad2(alarm.minute)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (alarm.enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                val subtitle = listOf(alarm.label.takeIf { it.isNotBlank() }, repeatSummary(alarm.repeatDays))
                    .filterNotNull()
                    .joinToString(" • ")
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = tokens.spacing.xs),
                )
            }
            Switch(checked = alarm.enabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete alarm")
            }
        }
        VelaButton(
            text = "Edit",
            onClick = onEdit,
            variant = VelaButtonVariant.Text,
            modifier = Modifier.padding(top = tokens.spacing.xs),
        )
    }
}

@Composable
private fun AlarmEditorDialog(
    draft: AlarmDraft,
    onTime: (Int, Int) -> Unit,
    onLabel: (String) -> Unit,
    onToggleDay: (WeekDay) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = LocalVelaTokens.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (draft.id == 0L) "New alarm" else "Edit alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.md)) {
                TimeStepper(hour = draft.hour, minute = draft.minute, onTime = onTime)
                OutlinedTextField(
                    value = draft.label,
                    onValueChange = onLabel,
                    label = { Text("Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Repeat", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
                    WeekDay.entries.forEach { day ->
                        FilterChip(
                            selected = day in draft.repeatDays,
                            onClick = { onToggleDay(day) },
                            label = { Text(day.shortLabel) },
                        )
                    }
                }
            }
        },
        confirmButton = { VelaButton(text = "Save", onClick = onSave) },
        dismissButton = { VelaButton(text = "Cancel", onClick = onDismiss, variant = VelaButtonVariant.Text) },
    )
}

private const val HOURS_IN_DAY = 24
private const val MINUTES_IN_HOUR = 60
private const val MINUTE_STEP = 5

@Composable
private fun TimeStepper(hour: Int, minute: Int, onTime: (Int, Int) -> Unit) {
    val tokens = LocalVelaTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Stepper(
            label = "Hour",
            value = pad2(hour),
            onDecrement = { onTime((hour + HOURS_IN_DAY - 1) % HOURS_IN_DAY, minute) },
            onIncrement = { onTime((hour + 1) % HOURS_IN_DAY, minute) },
            modifier = Modifier.weight(1f),
        )
        Stepper(
            label = "Minute",
            value = pad2(minute),
            onDecrement = { onTime(hour, (minute + MINUTES_IN_HOUR - MINUTE_STEP) % MINUTES_IN_HOUR) },
            onIncrement = { onTime(hour, (minute + MINUTE_STEP) % MINUTES_IN_HOUR) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Stepper(
    label: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            VelaButton(text = "−", onClick = onDecrement, variant = VelaButtonVariant.Tonal)
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            VelaButton(text = "+", onClick = onIncrement, variant = VelaButtonVariant.Tonal)
        }
    }
}
