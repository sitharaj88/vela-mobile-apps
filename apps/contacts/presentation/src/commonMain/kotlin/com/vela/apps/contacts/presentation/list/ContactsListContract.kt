/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.presentation.list

import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.domain.ContactSection
import com.vela.apps.contacts.domain.groupIntoSections
import com.vela.apps.contacts.domain.searchContacts

/** Which subset of contacts the list is showing. */
enum class ContactsTab { All, Favorites }

data class ContactsListState(
    val query: String = "",
    val tab: ContactsTab = ContactsTab.All,
    val all: List<Contact> = emptyList(),
    val isLoading: Boolean = true,
    val canEdit: Boolean = false,
    val canImport: Boolean = false,
) {
    /** Contacts after the active tab + search filters, grouped into alphabetical sections. */
    val sections: List<ContactSection> get() {
        val byTab = if (tab == ContactsTab.Favorites) all.filter { it.isFavorite } else all
        return groupIntoSections(searchContacts(byTab, query))
    }

    val isEmpty: Boolean get() = !isLoading && sections.isEmpty()
    val favoritesCount: Int get() = all.count { it.isFavorite }
}

sealed interface ContactsListIntent {
    data class Search(val query: String) : ContactsListIntent
    data class SelectTab(val tab: ContactsTab) : ContactsListIntent
    data class ToggleFavorite(val contactId: String, val favorite: Boolean) : ContactsListIntent
    data class ImportVCard(val text: String) : ContactsListIntent
}

sealed interface ContactsListEffect {
    data class ShowMessage(val text: String) : ContactsListEffect
}
