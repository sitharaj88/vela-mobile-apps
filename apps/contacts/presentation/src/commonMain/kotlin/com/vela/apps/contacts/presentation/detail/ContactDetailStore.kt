/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.presentation.detail

import androidx.lifecycle.viewModelScope
import com.vela.apps.contacts.domain.ContactsRepository
import com.vela.core.common.MviStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Observes a single contact by [contactId], reflecting favorite changes live, and supports starring
 * and (on editable platforms) deletion.
 */
class ContactDetailStore(
    private val contactId: String,
    private val repository: ContactsRepository,
) : MviStore<ContactDetailState, ContactDetailIntent, ContactDetailEffect>(
    ContactDetailState(canEdit = repository.canEdit),
) {

    init {
        viewModelScope.launch {
            repository.observeContact(contactId)
                .catch { setState { copy(isLoading = false) } }
                .collect { contact ->
                    setState { copy(contact = contact, isLoading = false) }
                }
        }
    }

    override fun onIntent(intent: ContactDetailIntent) {
        when (intent) {
            is ContactDetailIntent.SetFavorite -> viewModelScope.launch {
                repository.setFavorite(contactId, intent.favorite)
            }
            ContactDetailIntent.Delete -> viewModelScope.launch {
                if (repository.canEdit) {
                    repository.delete(contactId)
                    emitEffect(ContactDetailEffect.Deleted)
                }
            }
        }
    }
}
