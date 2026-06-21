/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data

import com.vela.apps.contacts.data.local.ContactsDatabase
import com.vela.apps.contacts.data.local.buildContactsDatabase
import com.vela.apps.contacts.data.local.contactsPlatformDatabaseModule
import com.vela.apps.contacts.domain.ContactsRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Desktop has no system address book, so Vela provides a real Room-backed editable store. */
actual fun contactsPlatformModule(): Module = module {
    includes(contactsPlatformDatabaseModule())
    single { buildContactsDatabase(get()) }
    single { get<ContactsDatabase>().contactDao() }
    singleOf(::RoomContactsRepository) bind ContactsRepository::class
}
