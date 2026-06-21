/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.presentation.detail.ContactDetailEffect
import com.vela.apps.contacts.presentation.detail.ContactDetailIntent
import com.vela.apps.contacts.presentation.detail.ContactDetailStore
import com.vela.apps.contacts.ui.platform.ContactActions
import com.vela.apps.contacts.ui.platform.rememberContactActions
import com.vela.core.designsystem.component.VelaCard
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ContactDetailScreen(
    contactId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    store: ContactDetailStore = koinViewModel { parametersOf(contactId) },
) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current
    val contact = state.contact
    val actions = rememberContactActions()

    LaunchedEffect(store) {
        store.effects.collect { effect ->
            when (effect) {
                ContactDetailEffect.Deleted -> onBack()
            }
        }
    }

    VelaScaffold(
        title = contact?.displayName ?: "Contact",
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = onBack,
        actions = {
            if (contact != null) {
                IconButton(
                    onClick = { store.onIntent(ContactDetailIntent.SetFavorite(!contact.isFavorite)) },
                ) {
                    if (contact.isFavorite) {
                        Icon(Icons.Filled.Star, "Unfavorite", tint = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Outlined.StarBorder, "Favorite")
                    }
                }
                if (state.canEdit) {
                    IconButton(onClick = { onEdit(contact.id) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { store.onIntent(ContactDetailIntent.Delete) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
        },
    ) { padding ->
        if (state.isMissing) {
            VelaEmptyState(
                modifier = Modifier.padding(padding),
                icon = Icons.Filled.PersonOff,
                title = "Contact not found",
                description = "It may have been removed.",
            )
        } else if (contact != null) {
            ContactDetailContent(
                contact = contact,
                actions = actions,
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = tokens.spacing.lg),
            )
        }
    }
}

@Composable
private fun ContactDetailContent(contact: Contact, actions: ContactActions, modifier: Modifier = Modifier) {
    val tokens = LocalVelaTokens.current
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.lg),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = tokens.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            MonogramAvatar(contact.displayName, size = AVATAR_SIZE)
            Text(text = contact.displayName, style = MaterialTheme.typography.headlineSmall)
            contact.organization?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }

        if (contact.phoneNumbers.isNotEmpty()) {
            ContactSection(
                title = "Phone",
                icon = Icons.Filled.Phone,
                values = contact.phoneNumbers,
                onValueClick = if (actions.canDialAndEmail) actions::dial else null,
            )
        }
        if (contact.emails.isNotEmpty()) {
            ContactSection(
                title = "Email",
                icon = Icons.Filled.Email,
                values = contact.emails,
                onValueClick = actions::email,
            )
        }
        if (contact.organization != null) {
            ContactSection(
                title = "Organization",
                icon = Icons.Filled.Business,
                values = listOfNotNull(contact.organization),
                onValueClick = null,
            )
        }
    }
}

@Composable
private fun ContactSection(
    title: String,
    icon: ImageVector,
    values: List<String>,
    onValueClick: ((String) -> Unit)?,
) {
    val tokens = LocalVelaTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        values.forEach { value ->
            val rowModifier = Modifier.fillMaxWidth().let { base ->
                if (onValueClick != null) base.clickable { onValueClick(value) } else base
            }
            VelaCard(modifier = rowModifier) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = tokens.spacing.md),
                    )
                }
            }
        }
    }
}

private val AVATAR_SIZE = 72.dp
