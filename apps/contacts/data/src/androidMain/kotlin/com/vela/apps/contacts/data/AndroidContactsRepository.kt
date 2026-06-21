/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.vela.apps.contacts.data.local.FavoriteDao
import com.vela.apps.contacts.data.local.FavoriteEntity
import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.domain.ContactsRepository
import com.vela.apps.contacts.domain.searchContacts
import com.vela.core.common.platformIoDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Reads device contacts via `ContactsContract` (read-only in v1) and overlays starred state from a
 * local Room [FavoriteDao]. Requires the `READ_CONTACTS` runtime permission; without it the queries
 * return empty cursors and the list is empty (the app degrades gracefully).
 */
class AndroidContactsRepository(
    private val context: Context,
    private val favoriteDao: FavoriteDao,
) : ContactsRepository {

    override val canEdit: Boolean = false
    override val canImport: Boolean = false

    override fun observeContacts(): Flow<List<Contact>> =
        favoriteDao.observeIds().map { favorites -> readContacts(favorites.toSet()) }

    override fun observeContact(id: String): Flow<Contact?> =
        favoriteDao.observeIds().map { favorites -> readContacts(favorites.toSet()).firstOrNull { it.id == id } }

    override suspend fun listContacts(): List<Contact> = readContacts(favoriteDao.ids().toSet())

    override suspend fun byId(id: String): Contact? = listContacts().firstOrNull { it.id == id }

    override suspend fun search(query: String): List<Contact> = searchContacts(listContacts(), query)

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        if (favorite) favoriteDao.add(FavoriteEntity(id)) else favoriteDao.remove(id)
    }

    override suspend fun create(contact: Contact): String =
        throw UnsupportedOperationException("System contacts are read-only on Android in v1")

    override suspend fun update(contact: Contact): Unit =
        throw UnsupportedOperationException("System contacts are read-only on Android in v1")

    override suspend fun delete(id: String): Unit =
        throw UnsupportedOperationException("System contacts are read-only on Android in v1")

    override suspend fun importVCard(text: String): Int =
        throw UnsupportedOperationException("vCard import is not supported on Android in v1")

    private suspend fun readContacts(favorites: Set<String>): List<Contact> =
        withContext(platformIoDispatcher()) {
            val resolver = context.contentResolver
            val phones = relationMap(
                resolver,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
            )
            val emails = relationMap(
                resolver,
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
            )
            val organizations = relationMap(
                resolver,
                ContactsContract.Data.CONTENT_URI,
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.CommonDataKinds.Organization.COMPANY,
                selection = "${ContactsContract.Data.MIMETYPE} = ?",
                selectionArgs = arrayOf(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
            )
            readContactRows(resolver, favorites, phones, emails, organizations)
        }

    private fun readContactRows(
        resolver: ContentResolver,
        favorites: Set<String>,
        phones: Map<String, List<String>>,
        emails: Map<String, List<String>>,
        organizations: Map<String, List<String>>,
    ): List<Contact> {
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI,
        )
        val sort = "${ContactsContract.Contacts.DISPLAY_NAME} COLLATE NOCASE ASC"
        val contacts = mutableListOf<Contact>()
        resolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, sort)
            ?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val photoIdx = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
                if (idIdx < 0 || nameIdx < 0) return@use
                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIdx) ?: continue
                    val name = cursor.getString(nameIdx).orEmpty()
                    if (name.isBlank()) continue
                    contacts += Contact(
                        id = id,
                        displayName = name,
                        phoneNumbers = phones[id].orEmpty(),
                        emails = emails[id].orEmpty(),
                        organization = organizations[id]?.firstOrNull(),
                        photoUri = if (photoIdx >= 0) cursor.getString(photoIdx) else null,
                        isFavorite = id in favorites,
                    )
                }
            }
        return contacts
    }

    private fun relationMap(
        resolver: ContentResolver,
        uri: Uri,
        idColumn: String,
        valueColumn: String,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
    ): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()
        resolver.query(uri, arrayOf(idColumn, valueColumn), selection, selectionArgs, null)?.use { cursor ->
            val idIdx = cursor.getColumnIndex(idColumn)
            val valueIdx = cursor.getColumnIndex(valueColumn)
            if (idIdx < 0 || valueIdx < 0) return@use
            collect(cursor, idIdx, valueIdx, result)
        }
        return result
    }

    private fun collect(
        cursor: Cursor,
        idIdx: Int,
        valueIdx: Int,
        target: MutableMap<String, MutableList<String>>,
    ) {
        while (cursor.moveToNext()) {
            val id = cursor.getString(idIdx) ?: continue
            val value = cursor.getString(valueIdx)?.trim().orEmpty()
            if (value.isEmpty()) continue
            val bucket = target.getOrPut(id) { mutableListOf() }
            if (value !in bucket) bucket += value
        }
    }
}
