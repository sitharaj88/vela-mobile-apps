/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.ui.di

import com.vela.apps.contacts.data.contactsPlatformModule
import com.vela.apps.contacts.presentation.detail.ContactDetailStore
import com.vela.apps.contacts.presentation.di.contactsPresentationModule
import com.vela.apps.contacts.presentation.editor.ContactEditorStore
import com.vela.apps.contacts.presentation.list.ContactsListStore
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole Contacts app. */
val contactsModule = module {
    includes(contactsPlatformModule(), contactsPresentationModule)
    viewModelOf(::ContactsListStore)
    // Detail and editor stores receive their contact id as a runtime parameter.
    viewModel { (contactId: String) -> ContactDetailStore(contactId, get()) }
    viewModel { (contactId: String) -> ContactEditorStore(contactId, get()) }
}
