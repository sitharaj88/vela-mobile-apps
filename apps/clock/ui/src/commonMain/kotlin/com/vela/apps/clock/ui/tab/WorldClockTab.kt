/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.clock.domain.WorldClockDisplay
import com.vela.apps.clock.presentation.world.PickableZone
import com.vela.apps.clock.presentation.world.WorldClockIntent
import com.vela.apps.clock.presentation.world.WorldClockStore
import com.vela.core.designsystem.component.VelaCard
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.theme.LocalVelaTokens

@Composable
fun WorldClockTab(store: WorldClockStore) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current

    Box(Modifier.fillMaxSize()) {
        if (state.rows.isEmpty()) {
            VelaEmptyState(
                icon = Icons.Filled.Public,
                title = "No cities",
                description = "Tap + to add a timezone.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(tokens.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            ) {
                items(state.rows, key = { it.zoneId }) { row ->
                    WorldRow(row = row, onRemove = { store.onIntent(WorldClockIntent.RemoveZone(row.zoneId)) })
                }
            }
        }

        FloatingActionButton(
            onClick = { store.onIntent(WorldClockIntent.OpenPicker) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(tokens.spacing.lg),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add city")
        }
    }

    if (state.picking) {
        ZonePickerDialog(
            zones = state.available,
            onPick = { store.onIntent(WorldClockIntent.AddZone(it)) },
            onDismiss = { store.onIntent(WorldClockIntent.ClosePicker) },
        )
    }
}

@Composable
private fun WorldRow(row: WorldClockDisplay, onRemove: () -> Unit) {
    val tokens = LocalVelaTokens.current
    VelaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(row.cityName, style = MaterialTheme.typography.titleMedium)
                val sub = listOf(row.offsetLabel, row.dayLabel).filter { it.isNotBlank() }.joinToString(" • ")
                Text(
                    sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = tokens.spacing.xs),
                )
            }
            Text(row.time, style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Remove city")
            }
        }
    }
}

@Composable
private fun ZonePickerDialog(
    zones: List<PickableZone>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add timezone") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(zones, key = { it.id }) { zone ->
                    TextButton(onClick = { onPick(zone.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "${zone.cityName}  (${zone.id})",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
