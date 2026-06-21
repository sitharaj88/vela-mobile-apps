/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data

import com.vela.apps.contacts.data.local.ContactsDatabase
import com.vela.apps.contacts.data.local.buildContactsDatabase
import com.vela.apps.contacts.data.local.contactsPlatformDatabaseModule
import com.vela.apps.contacts.domain.ContactsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Android reads system contacts through `ContactsContract` and keeps a local Room favorites overlay
 * (the system store is read-only in v1). Requires the `READ_CONTACTS` runtime permission.
 */
actual fun contactsPlatformModule(): Module = module {
    includes(contactsPlatformDatabaseModule())
    single { buildContactsDatabase(get()) }
    single { get<ContactsDatabase>().favoriteDao() }
    single { AndroidContactsRepository(androidContext(), get()) } bind ContactsRepository::class
}
