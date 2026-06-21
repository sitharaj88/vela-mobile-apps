/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.presentation.list.ContactsListEffect
import com.vela.apps.contacts.presentation.list.ContactsListIntent
import com.vela.apps.contacts.presentation.list.ContactsListStore
import com.vela.apps.contacts.presentation.list.ContactsTab
import com.vela.apps.contacts.ui.platform.rememberVCardPicker
import com.vela.core.designsystem.component.VelaCard
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import org.koin.compose.viewmodel.koinViewModel

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ContactsListScreen(
    onOpenContact: (String) -> Unit,
    onCreateContact: () -> Unit,
    store: ContactsListStore = koinViewModel(),
) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current
    val snackbarHostState = remember { SnackbarHostState() }
    val vCardPicker = rememberVCardPicker()

    LaunchedEffect(store) {
        store.effects.collect { effect ->
            when (effect) {
                is ContactsListEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    VelaScaffold(
        title = "Vela Contacts",
        snackbarHostState = snackbarHostState,
        actions = {
            if (state.canImport) {
                IconButton(onClick = { vCardPicker.pick { store.onIntent(ContactsListIntent.ImportVCard(it)) } }) {
                    Icon(Icons.Filled.FileUpload, contentDescription = "Import vCard")
                }
            }
        },
        floatingActionButton = {
            if (state.canEdit) {
                FloatingActionButton(onClick = onCreateContact) {
                    Icon(Icons.Filled.Add, contentDescription = "New contact")
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = tokens.spacing.lg)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { store.onIntent(ContactsListIntent.Search(it)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = tokens.spacing.sm),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Search name, phone, email") },
            )

            TabRow(selectedTabIndex = state.tab.ordinal) {
                Tab(
                    selected = state.tab == ContactsTab.All,
                    onClick = { store.onIntent(ContactsListIntent.SelectTab(ContactsTab.All)) },
                    text = { Text("All") },
                )
                Tab(
                    selected = state.tab == ContactsTab.Favorites,
                    onClick = { store.onIntent(ContactsListIntent.SelectTab(ContactsTab.Favorites)) },
                    text = { Text("Favorites (${state.favoritesCount})") },
                )
            }

            if (state.isEmpty) {
                EmptyContacts(query = state.query, tab = state.tab)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = tokens.spacing.sm),
                ) {
                    state.sections.forEach { section ->
                        item(key = section.key) { SectionHeader(section.key) }
                        items(section.contacts, key = { it.id }) { contact ->
                            ContactRow(
                                contact = contact,
                                onClick = { onOpenContact(contact.id) },
                                onToggleFavorite = {
                                    store.onIntent(
                                        ContactsListIntent.ToggleFavorite(contact.id, !contact.isFavorite),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyContacts(query: String, tab: ContactsTab) {
    val title = when {
        query.isNotBlank() -> "No matches"
        tab == ContactsTab.Favorites -> "No favorites yet"
        else -> "No contacts"
    }
    val description = when {
        query.isNotBlank() -> "Try a different search."
        tab == ContactsTab.Favorites -> "Star a contact to see it here."
        else -> "Contacts appear here once available, or add one with the + button."
    }
    VelaEmptyState(icon = Icons.Filled.Person, title = title, description = description)
}

@Composable
private fun SectionHeader(key: String) {
    val tokens = LocalVelaTokens.current
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = key,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = tokens.spacing.xs, horizontal = tokens.spacing.xs),
        )
    }
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    val tokens = LocalVelaTokens.current
    VelaCard(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = tokens.spacing.xs)
            .clickable(onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonogramAvatar(contact.displayName)
            Column(Modifier.weight(1f).padding(horizontal = tokens.spacing.md)) {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = contact.phoneNumbers.firstOrNull() ?: contact.emails.firstOrNull()
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onToggleFavorite) {
                if (contact.isFavorite) {
                    Icon(Icons.Filled.Star, contentDescription = "Unfavorite", tint = MaterialTheme.colorScheme.primary)
                } else {
                    Icon(Icons.Outlined.StarBorder, contentDescription = "Favorite")
                }
            }
        }
    }
}
