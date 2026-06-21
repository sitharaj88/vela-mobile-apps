/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.presentation

import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.domain.ContactsRepository
import com.vela.apps.contacts.domain.VCard
import com.vela.apps.contacts.domain.searchContacts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [ContactsRepository] for store tests. [canEdit]/[canImport] are configurable. */
class FakeContactsRepository(
    override val canEdit: Boolean = true,
    override val canImport: Boolean = true,
    initial: List<Contact> = emptyList(),
) : ContactsRepository {

    val contacts = MutableStateFlow(initial)
    private var nextId = initial.size + 1

    override fun observeContacts(): Flow<List<Contact>> = contacts

    override fun observeContact(id: String): Flow<Contact?> =
        contacts.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun listContacts(): List<Contact> = contacts.value

    override suspend fun byId(id: String): Contact? = contacts.value.firstOrNull { it.id == id }

    override suspend fun search(query: String): List<Contact> = searchContacts(contacts.value, query)

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        contacts.value = contacts.value.map { if (it.id == id) it.copy(isFavorite = favorite) else it }
    }

    override suspend fun create(contact: Contact): String {
        val id = (nextId++).toString()
        contacts.value = contacts.value + contact.copy(id = id)
        return id
    }

    override suspend fun update(contact: Contact) {
        contacts.value = contacts.value.map { if (it.id == contact.id) contact else it }
    }

    override suspend fun delete(id: String) {
        contacts.value = contacts.value.filterNot { it.id == id }
    }

    override suspend fun importVCard(text: String): Int {
        val parsed = VCard.parse(text)
        parsed.forEach { create(it) }
        return parsed.size
    }
}
