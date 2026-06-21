/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.draw.presentation.DrawEffect
import com.vela.apps.draw.presentation.DrawIntent
import com.vela.apps.draw.presentation.DrawStore
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

/** Root composable for Draw. */
@Composable
fun DrawApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Rose, themeMode = themeMode, dynamicColor = true) {
        val store: DrawStore = koinViewModel()
        val state by store.state.collectAsStateWithLifecycle()
        val snackbar = remember { SnackbarHostState() }

        // Latest reported canvas pixel size — used to render the export at the on-screen resolution.
        var canvasWidth by remember { mutableStateOf(0) }
        var canvasHeight by remember { mutableStateOf(0) }

        DrawEffects(store, snackbar)

        VelaScaffold(
            title = "Vela Draw",
            snackbarHostState = snackbar,
            actions = {
                IconButton(onClick = { store.onIntent(DrawIntent.Undo) }, enabled = state.canUndo) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                }
                IconButton(onClick = { store.onIntent(DrawIntent.Redo) }, enabled = state.canRedo) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                }
                IconButton(onClick = { store.onIntent(DrawIntent.Clear) }, enabled = state.canUndo) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear")
                }
                IconButton(
                    onClick = { store.onIntent(DrawIntent.Export(canvasWidth, canvasHeight)) },
                    enabled = !state.isExporting,
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Export PNG")
                }
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                DrawCanvas(
                    state = state,
                    onCommitStroke = { points -> store.onIntent(DrawIntent.CommitStroke(points)) },
                    onCommitShape = { start, end -> store.onIntent(DrawIntent.CommitShape(start, end)) },
                    onSize = { w, h -> canvasWidth = w; canvasHeight = h },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
                DrawToolbar(state = state, onIntent = store::onIntent)
            }
        }
    }
}

@Composable
private fun DrawEffects(store: DrawStore, snackbar: SnackbarHostState) {
    LaunchedEffect(store) {
        store.effects.collectLatest { effect ->
            val message = when (effect) {
                is DrawEffect.Exported -> "Saved to ${effect.location}"
                is DrawEffect.ExportFailed -> "Export failed: ${effect.message}"
            }
            snackbar.showSnackbar(message)
        }
    }
}
