/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data

import com.vela.apps.contacts.data.local.FavoriteDao
import com.vela.apps.contacts.data.local.FavoriteEntity
import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.domain.ContactsRepository
import com.vela.apps.contacts.domain.searchContacts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * iOS contacts repository. Favorites are stored locally via Room (functional). Reading the system
 * address book uses `CNContactStore`; the cinterop enumeration is environment-specific and only
 * compiles/links on macOS, so it is stubbed here and must be wired on a Mac build.
 */
class IosContactsRepository(
    private val favoriteDao: FavoriteDao,
) : ContactsRepository {

    override val canEdit: Boolean = false
    override val canImport: Boolean = false

    override fun observeContacts(): Flow<List<Contact>> =
        favoriteDao.observeIds().map { favorites -> readSystemContacts(favorites.toSet()) }

    override fun observeContact(id: String): Flow<Contact?> =
        favoriteDao.observeIds().map { favorites ->
            readSystemContacts(favorites.toSet()).firstOrNull { it.id == id }
        }

    override suspend fun listContacts(): List<Contact> = readSystemContacts(favoriteDao.ids().toSet())

    override suspend fun byId(id: String): Contact? = listContacts().firstOrNull { it.id == id }

    override suspend fun search(query: String): List<Contact> = searchContacts(listContacts(), query)

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        if (favorite) favoriteDao.add(FavoriteEntity(id)) else favoriteDao.remove(id)
    }

    override suspend fun create(contact: Contact): String =
        throw UnsupportedOperationException("System contacts are read-only on iOS in v1")

    override suspend fun update(contact: Contact): Unit =
        throw UnsupportedOperationException("System contacts are read-only on iOS in v1")

    override suspend fun delete(id: String): Unit =
        throw UnsupportedOperationException("System contacts are read-only on iOS in v1")

    override suspend fun importVCard(text: String): Int =
        throw UnsupportedOperationException("vCard import is not supported on iOS in v1")

    // TODO(ios): request CNContactStore access (requestAccessForEntityType) and enumerate
    //  CNContact givenName/familyName/phoneNumbers(CNPhoneNumber.stringValue)/emailAddresses,
    //  mapping CNContact.identifier -> Contact.id. Requires the macOS toolchain + the Contacts
    //  framework cinterop, which is unavailable in this build, so we return the favorites set only.
    @Suppress("UnusedPrivateMember")
    private fun readSystemContacts(favorites: Set<String>): List<Contact> = emptyList()
}
