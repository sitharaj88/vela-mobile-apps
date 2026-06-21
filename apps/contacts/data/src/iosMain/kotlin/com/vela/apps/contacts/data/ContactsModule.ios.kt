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
import org.koin.dsl.bind
import org.koin.dsl.module

/** iOS reads `CNContactStore` best-effort with a local Room favorites overlay. */
actual fun contactsPlatformModule(): Module = module {
    includes(contactsPlatformDatabaseModule())
    single { buildContactsDatabase(get()) }
    single { get<ContactsDatabase>().favoriteDao() }
    single { IosContactsRepository(get()) } bind ContactsRepository::class
}
