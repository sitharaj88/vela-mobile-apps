/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.presentation.list

import androidx.lifecycle.viewModelScope
import com.vela.apps.contacts.domain.ContactsRepository
import com.vela.core.common.MviStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Observes the contact list and exposes the search query, the active tab (All/Favorites), and the
 * platform's edit/import capabilities. Alphabetical sectioning happens in [ContactsListState].
 */
class ContactsListStore(
    private val repository: ContactsRepository,
) : MviStore<ContactsListState, ContactsListIntent, ContactsListEffect>(
    ContactsListState(canEdit = repository.canEdit, canImport = repository.canImport),
) {

    init {
        viewModelScope.launch {
            repository.observeContacts()
                .catch { setState { copy(isLoading = false) } }
                .collect { contacts ->
                    setState { copy(all = contacts, isLoading = false) }
                }
        }
    }

    override fun onIntent(intent: ContactsListIntent) {
        when (intent) {
            is ContactsListIntent.Search -> setState { copy(query = intent.query) }
            is ContactsListIntent.SelectTab -> setState { copy(tab = intent.tab) }
            is ContactsListIntent.ToggleFavorite -> viewModelScope.launch {
                repository.setFavorite(intent.contactId, intent.favorite)
            }
            is ContactsListIntent.ImportVCard -> importVCard(intent.text)
        }
    }

    private fun importVCard(text: String) {
        viewModelScope.launch {
            val added = runCatching { repository.importVCard(text) }.getOrDefault(0)
            val message = if (added > 0) "Imported $added contact(s)" else "No contacts found in file"
            emitEffect(ContactsListEffect.ShowMessage(message))
        }
    }
}
