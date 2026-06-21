/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.presentation

import app.cash.turbine.test
import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.presentation.editor.ContactEditorEffect
import com.vela.apps.contacts.presentation.editor.ContactEditorIntent
import com.vela.apps.contacts.presentation.editor.ContactEditorStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactEditorStoreTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeContactsRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeContactsRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun new_contact_starts_blank() = runTest(dispatcher) {
        val store = ContactEditorStore("", repository)
        testScheduler.advanceUntilIdle()
        assertTrue(store.state.value.isNew)
        assertEquals(false, store.state.value.canSave)
    }

    @Test
    fun save_creates_contact_with_parsed_phone_lines() = runTest(dispatcher) {
        val store = ContactEditorStore("", repository)
        testScheduler.advanceUntilIdle()
        store.onIntent(ContactEditorIntent.NameChanged("New Person"))
        store.onIntent(ContactEditorIntent.PhonesChanged("111\n222"))
        store.onIntent(ContactEditorIntent.OrganizationChanged("Acme"))
        store.effects.test {
            store.onIntent(ContactEditorIntent.Save)
            testScheduler.advanceUntilIdle()
            assertTrue(awaitItem() is ContactEditorEffect.Saved)
        }
        val saved = repository.contacts.value.single()
        assertEquals("New Person", saved.displayName)
        assertEquals(listOf("111", "222"), saved.phoneNumbers)
        assertEquals("Acme", saved.organization)
    }

    @Test
    fun loads_existing_contact_for_editing() = runTest(dispatcher) {
        repository.create(Contact(id = "x", displayName = "Old Name", phoneNumbers = listOf("555")))
        val id = repository.contacts.value.single().id
        val store = ContactEditorStore(id, repository)
        testScheduler.advanceUntilIdle()
        assertEquals("Old Name", store.state.value.name)
        assertEquals("555", store.state.value.phones)
        assertEquals(false, store.state.value.isNew)
    }
}
