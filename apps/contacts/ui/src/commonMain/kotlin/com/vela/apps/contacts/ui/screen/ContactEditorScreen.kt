/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.contacts.presentation.editor.ContactEditorEffect
import com.vela.apps.contacts.presentation.editor.ContactEditorIntent
import com.vela.apps.contacts.presentation.editor.ContactEditorStore
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ContactEditorScreen(
    contactId: String,
    onDone: () -> Unit,
    store: ContactEditorStore = koinViewModel { parametersOf(contactId) },
) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current

    LaunchedEffect(store) {
        store.effects.collect { effect ->
            when (effect) {
                ContactEditorEffect.Saved -> onDone()
            }
        }
    }

    VelaScaffold(
        title = if (state.isNew) "New contact" else "Edit contact",
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = onDone,
        actions = {
            IconButton(
                onClick = { store.onIntent(ContactEditorIntent.Save) },
                enabled = state.canSave,
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Save")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .padding(horizontal = tokens.spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { store.onIntent(ContactEditorIntent.NameChanged(it)) },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = tokens.spacing.md),
            )
            OutlinedTextField(
                value = state.organization,
                onValueChange = { store.onIntent(ContactEditorIntent.OrganizationChanged(it)) },
                label = { Text("Organization") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.phones,
                onValueChange = { store.onIntent(ContactEditorIntent.PhonesChanged(it)) },
                label = { Text("Phone numbers (one per line)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.emails,
                onValueChange = { store.onIntent(ContactEditorIntent.EmailsChanged(it)) },
                label = { Text("Emails (one per line)") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
