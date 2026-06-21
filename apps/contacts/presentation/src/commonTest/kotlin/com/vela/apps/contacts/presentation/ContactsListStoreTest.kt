/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.presentation

import app.cash.turbine.test
import com.vela.apps.contacts.domain.Contact
import com.vela.apps.contacts.presentation.list.ContactsListEffect
import com.vela.apps.contacts.presentation.list.ContactsListIntent
import com.vela.apps.contacts.presentation.list.ContactsListStore
import com.vela.apps.contacts.presentation.list.ContactsTab
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
class ContactsListStoreTest {

    private val dispatcher = StandardTestDispatcher()

    private val sample = listOf(
        Contact(id = "1", displayName = "Ada Lovelace", isFavorite = true),
        Contact(id = "2", displayName = "Alan Turing"),
        Contact(id = "3", displayName = "Brian Kernighan"),
    )

    private lateinit var repository: FakeContactsRepository
    private lateinit var store: ContactsListStore

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeContactsRepository(initial = sample)
        store = ContactsListStore(repository)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loads_contacts_and_groups_into_sections() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        val state = store.state.value
        assertEquals(false, state.isLoading)
        assertEquals(listOf("A", "B"), state.sections.map { it.key })
        assertEquals(2, state.sections.first().contacts.size)
        assertEquals(1, state.favoritesCount)
    }

    @Test
    fun reflects_repository_capabilities() = runTest(dispatcher) {
        assertTrue(store.state.value.canEdit)
        assertTrue(store.state.value.canImport)
    }

    @Test
    fun search_filters_sections() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(ContactsListIntent.Search("turing"))
        val sections = store.state.value.sections
        assertEquals(1, sections.size)
        assertEquals("Alan Turing", sections.single().contacts.single().displayName)
    }

    @Test
    fun favorites_tab_shows_only_favorites() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(ContactsListIntent.SelectTab(ContactsTab.Favorites))
        val names = store.state.value.sections.flatMap { it.contacts }.map { it.displayName }
        assertEquals(listOf("Ada Lovelace"), names)
    }

    @Test
    fun toggle_favorite_updates_state() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.onIntent(ContactsListIntent.ToggleFavorite("2", favorite = true))
        testScheduler.advanceUntilIdle()
        assertEquals(2, store.state.value.favoritesCount)
    }

    @Test
    fun import_vcard_adds_contacts_and_emits_message() = runTest(dispatcher) {
        testScheduler.advanceUntilIdle()
        store.effects.test {
            store.onIntent(
                ContactsListIntent.ImportVCard("BEGIN:VCARD\nFN:Carol Imported\nEND:VCARD"),
            )
            testScheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is ContactsListEffect.ShowMessage)
            assertEquals("Imported 1 contact(s)", (effect as ContactsListEffect.ShowMessage).text)
        }
        assertTrue(store.state.value.all.any { it.displayName == "Carol Imported" })
    }
}
