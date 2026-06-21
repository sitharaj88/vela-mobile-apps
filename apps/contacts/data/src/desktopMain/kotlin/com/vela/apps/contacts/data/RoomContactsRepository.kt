/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data

import com.vela.apps.contacts.data.local.ContactDao
import com.vela.apps.contacts.data.local.ContactEntity
import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.domain.ContactsRepository
import com.vela.apps.contacts.domain.VCard
import com.vela.apps.contacts.domain.searchContacts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed [ContactsRepository] for Desktop: a real, editable local address book that also
 * supports importing contacts from a vCard (.vcf) file.
 */
class RoomContactsRepository(
    private val dao: ContactDao,
) : ContactsRepository {

    override val canEdit: Boolean = true
    override val canImport: Boolean = true

    override fun observeContacts(): Flow<List<Contact>> =
        dao.observeAll().map { rows -> rows.map(ContactEntity::toDomain) }

    override fun observeContact(id: String): Flow<Contact?> =
        dao.observeById(id.toLong()).map { it?.toDomain() }

    override suspend fun listContacts(): List<Contact> = dao.list().map(ContactEntity::toDomain)

    override suspend fun byId(id: String): Contact? = dao.findById(id.toLong())?.toDomain()

    override suspend fun search(query: String): List<Contact> = searchContacts(listContacts(), query)

    override suspend fun setFavorite(id: String, favorite: Boolean) = dao.setFavorite(id.toLong(), favorite)

    override suspend fun create(contact: Contact): String =
        dao.upsert(contact.toEntity(id = 0)).toString()

    override suspend fun update(contact: Contact) {
        dao.upsert(contact.toEntity(id = contact.id.toLongOrNull() ?: 0))
    }

    override suspend fun delete(id: String) = dao.deleteById(id.toLong())

    override suspend fun importVCard(text: String): Int {
        val parsed = VCard.parse(text)
        parsed.forEach { dao.upsert(it.toEntity(id = 0)) }
        return parsed.size
    }
}

private const val SEPARATOR = "\n"

private fun ContactEntity.toDomain() = Contact(
    id = id.toString(),
    displayName = displayName,
    phoneNumbers = phones.splitValues(),
    emails = emails.splitValues(),
    organization = organization,
    isFavorite = isFavorite,
)

private fun Contact.toEntity(id: Long) = ContactEntity(
    id = id,
    displayName = displayName,
    phones = phoneNumbers.joinToString(SEPARATOR),
    emails = emails.joinToString(SEPARATOR),
    organization = organization,
    isFavorite = isFavorite,
)

private fun String.splitValues(): List<String> =
    if (isEmpty()) emptyList() else split(SEPARATOR).filter { it.isNotBlank() }
