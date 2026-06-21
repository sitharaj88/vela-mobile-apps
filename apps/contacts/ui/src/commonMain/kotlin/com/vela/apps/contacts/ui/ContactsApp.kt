/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vela.apps.contacts.ui.screen.ContactDetailScreen
import com.vela.apps.contacts.ui.screen.ContactEditorScreen
import com.vela.apps.contacts.ui.screen.ContactsListScreen
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import kotlinx.serialization.Serializable

/** Type-safe navigation routes for the Contacts app. */
object ContactsRoutes {
    @Serializable
    object List

    @Serializable
    data class Detail(val contactId: String)

    /** A blank [contactId] opens the editor in "create" mode. */
    @Serializable
    data class Editor(val contactId: String = "")
}

/**
 * Root composable for Contacts. Platforms call this after starting Koin with
 * [com.vela.apps.contacts.ui.di.contactsModule].
 */
@Composable
fun ContactsApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Cyan, themeMode = themeMode, dynamicColor = true) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = ContactsRoutes.List) {
            composable<ContactsRoutes.List> {
                ContactsListScreen(
                    onOpenContact = { id -> navController.navigate(ContactsRoutes.Detail(id)) },
                    onCreateContact = { navController.navigate(ContactsRoutes.Editor()) },
                )
            }
            composable<ContactsRoutes.Detail> { backStackEntry ->
                val route = backStackEntry.toRoute<ContactsRoutes.Detail>()
                ContactDetailScreen(
                    contactId = route.contactId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(ContactsRoutes.Editor(id)) },
                )
            }
            composable<ContactsRoutes.Editor> { backStackEntry ->
                val route = backStackEntry.toRoute<ContactsRoutes.Editor>()
                ContactEditorScreen(
                    contactId = route.contactId,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
