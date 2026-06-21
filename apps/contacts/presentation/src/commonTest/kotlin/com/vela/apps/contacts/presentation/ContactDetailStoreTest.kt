/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.presentation

import app.cash.turbine.test
import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.presentation.detail.ContactDetailEffect
import com.vela.apps.contacts.presentation.detail.ContactDetailIntent
import com.vela.apps.contacts.presentation.detail.ContactDetailStore
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactDetailStoreTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeContactsRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeContactsRepository(
            initial = listOf(Contact(id = "1", displayName = "Ada Lovelace")),
        )
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loads_contact_by_id() = runTest(dispatcher) {
        val store = ContactDetailStore("1", repository)
        testScheduler.advanceUntilIdle()
        assertEquals("Ada Lovelace", store.state.value.contact?.displayName)
        assertTrue(store.state.value.canEdit)
    }

    @Test
    fun missing_contact_sets_missing_flag() = runTest(dispatcher) {
        val store = ContactDetailStore("999", repository)
        testScheduler.advanceUntilIdle()
        assertTrue(store.state.value.isMissing)
        assertNull(store.state.value.contact)
    }

    @Test
    fun set_favorite_updates_observed_contact() = runTest(dispatcher) {
        val store = ContactDetailStore("1", repository)
        testScheduler.advanceUntilIdle()
        store.onIntent(ContactDetailIntent.SetFavorite(true))
        testScheduler.advanceUntilIdle()
        assertEquals(true, store.state.value.contact?.isFavorite)
    }

    @Test
    fun delete_removes_contact_and_emits_effect() = runTest(dispatcher) {
        val store = ContactDetailStore("1", repository)
        testScheduler.advanceUntilIdle()
        store.effects.test {
            store.onIntent(ContactDetailIntent.Delete)
            testScheduler.advanceUntilIdle()
            assertTrue(awaitItem() is ContactDetailEffect.Deleted)
        }
        assertTrue(repository.contacts.value.isEmpty())
    }
}
