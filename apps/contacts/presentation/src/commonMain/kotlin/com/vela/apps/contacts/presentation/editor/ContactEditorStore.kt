/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.presentation.editor

import androidx.lifecycle.viewModelScope
import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.domain.ContactsRepository
import com.vela.core.common.MviStore
import kotlinx.coroutines.launch

/**
 * Creates or edits a contact on editable platforms (Desktop). A blank [contactId] starts a new
 * contact; otherwise the existing one is loaded. Phone numbers and emails are edited as one entry
 * per line.
 */
class ContactEditorStore(
    private val contactId: String,
    private val repository: ContactsRepository,
) : MviStore<ContactEditorState, ContactEditorIntent, ContactEditorEffect>(ContactEditorState()) {

    init {
        viewModelScope.launch {
            val existing = contactId.takeIf { it.isNotEmpty() }?.let { repository.byId(it) }
            setState {
                if (existing == null) {
                    copy(isLoading = false)
                } else {
                    copy(
                        id = existing.id,
                        name = existing.displayName,
                        phones = existing.phoneNumbers.joinToString("\n"),
                        emails = existing.emails.joinToString("\n"),
                        organization = existing.organization.orEmpty(),
                        isLoading = false,
                    )
                }
            }
        }
    }

    override fun onIntent(intent: ContactEditorIntent) {
        when (intent) {
            is ContactEditorIntent.NameChanged -> setState { copy(name = intent.value) }
            is ContactEditorIntent.PhonesChanged -> setState { copy(phones = intent.value) }
            is ContactEditorIntent.EmailsChanged -> setState { copy(emails = intent.value) }
            is ContactEditorIntent.OrganizationChanged -> setState { copy(organization = intent.value) }
            ContactEditorIntent.Save -> save()
        }
    }

    private fun save() {
        val state = currentState
        if (!state.canSave) return
        viewModelScope.launch {
            val contact = Contact(
                id = state.id,
                displayName = state.name.trim(),
                phoneNumbers = state.phones.toLines(),
                emails = state.emails.toLines(),
                organization = state.organization.trim().takeIf { it.isNotBlank() },
            )
            if (state.isNew) repository.create(contact) else repository.update(contact)
            emitEffect(ContactEditorEffect.Saved)
        }
    }

    private fun String.toLines(): List<String> =
        split("\n", ",").map { it.trim() }.filter { it.isNotEmpty() }
}
