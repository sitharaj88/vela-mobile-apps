/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.notes.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vela.apps.notes.ui.screen.NoteEditorScreen
import com.vela.apps.notes.ui.screen.NotesListScreen
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import kotlinx.serialization.Serializable

/** Type-safe navigation routes for the Notes app. */
object NotesRoutes {
    @Serializable
    object List

    @Serializable
    data class Editor(val noteId: Long, val checklist: Boolean = false)
}

/**
 * Root composable for Notes. Platforms call this after starting Koin with
 * [com.vela.apps.notes.ui.di.notesModule].
 */
@Composable
fun NotesApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Amber, themeMode = themeMode, dynamicColor = true) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = NotesRoutes.List) {
            composable<NotesRoutes.List> {
                NotesListScreen(
                    onOpenNote = { id, checklist ->
                        navController.navigate(NotesRoutes.Editor(id, checklist))
                    },
                )
            }
            composable<NotesRoutes.Editor> { backStackEntry ->
                val route = backStackEntry.toRoute<NotesRoutes.Editor>()
                NoteEditorScreen(
                    noteId = route.noteId,
                    checklist = route.checklist,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
