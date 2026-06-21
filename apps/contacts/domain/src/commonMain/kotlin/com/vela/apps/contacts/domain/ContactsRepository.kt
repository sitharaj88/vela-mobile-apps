/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.domain

import kotlinx.coroutines.flow.Flow

/**
 * Reads and (where supported) writes contacts for the host platform.
 *
 * - **Android** reads `ContactsContract` (read-only system contacts); favorites are stored locally,
 *   so [setFavorite] works but [create]/[update]/[delete] are not supported ([canEdit] is `false`).
 * - **Desktop** is backed by a local Room store with full editing and vCard import ([canEdit] is `true`).
 * - **iOS** reads `CNContactStore` best-effort; favorites are local.
 */
interface ContactsRepository {

    /** Whether this platform supports creating, updating, and deleting contacts. */
    val canEdit: Boolean

    /** Whether this platform can import contacts from a vCard (.vcf) file. */
    val canImport: Boolean

    /** Streams the full contact list, sorted by display name, reacting to edits/favorites. */
    fun observeContacts(): Flow<List<Contact>>

    /** Streams a single contact by [id], emitting `null` when it is absent. */
    fun observeContact(id: String): Flow<Contact?>

    /** One-shot snapshot of all contacts (used by platforms without reactive sources). */
    suspend fun listContacts(): List<Contact>

    /** Returns the contact with [id], or `null`. */
    suspend fun byId(id: String): Contact?

    /** Returns contacts whose name/phone/email/organization match [query] (see [contactMatches]). */
    suspend fun search(query: String): List<Contact>

    /** Stars or unstars [id]. Supported on every platform (stored locally where needed). */
    suspend fun setFavorite(id: String, favorite: Boolean)

    /** Creates a new contact and returns its id. Only valid when [canEdit]. */
    suspend fun create(contact: Contact): String

    /** Updates an existing contact. Only valid when [canEdit]. */
    suspend fun update(contact: Contact)

    /** Deletes the contact with [id]. Only valid when [canEdit]. */
    suspend fun delete(id: String)

    /** Imports contacts parsed from raw vCard text, returning how many were added. Only when [canImport]. */
    suspend fun importVCard(text: String): Int
}
