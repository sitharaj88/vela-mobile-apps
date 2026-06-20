/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.presentation

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.vela.apps.calculator.domain.model.HistoryEntry
import com.vela.apps.calculator.domain.repository.HistoryRepository
import com.vela.apps.calculator.domain.usecase.EvaluateExpressionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorStoreTest {

    private val dispatcher = kotlinx.coroutines.test.StandardTestDispatcher()

    private class FakeHistoryRepository : HistoryRepository {
        val entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
        override fun observeHistory(): Flow<List<HistoryEntry>> = entries
        override suspend fun add(expression: String, result: String) {
            val id = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1L
            entries.value = listOf(HistoryEntry(id, expression, result, Clock.System.now())) + entries.value
        }
        override suspend fun clear() { entries.value = emptyList() }
    }

    private lateinit var repository: FakeHistoryRepository
    private lateinit var store: CalculatorStore

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeHistoryRepository()
        store = CalculatorStore(EvaluateExpressionUseCase(), repository)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun appending_tokens_builds_expression_and_live_preview() = runTest(dispatcher) {
        store.onIntent(CalculatorIntent.Append("2"))
        store.onIntent(CalculatorIntent.Append("+"))
        store.onIntent(CalculatorIntent.Append("3"))

        val state = store.state.value
        assertEquals("2+3", state.expression)
        assertEquals("5", state.preview)
        assertEquals(false, state.isError)
    }

    @Test
    fun evaluate_commits_result_and_records_history() = runTest(dispatcher) {
        listOf("6", "*", "7").forEach { store.onIntent(CalculatorIntent.Append(it)) }
        store.onIntent(CalculatorIntent.Evaluate)
        testScheduler.advanceUntilIdle()

        assertEquals("42", store.state.value.expression)
        assertEquals(1, repository.entries.value.size)
        assertEquals("42", repository.entries.value.first().result)
    }

    @Test
    fun divide_by_zero_sets_error_and_emits_effect() = runTest(dispatcher) {
        store.effects.test {
            listOf("5", "/", "0").forEach { store.onIntent(CalculatorIntent.Append(it)) }
            store.onIntent(CalculatorIntent.Evaluate)
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is CalculatorEffect.ShowError)
            assertTrue(store.state.value.isError)
        }
    }

    @Test
    fun clear_resets_expression_but_keeps_history() = runTest(dispatcher) {
        listOf("1", "+", "1").forEach { store.onIntent(CalculatorIntent.Append(it)) }
        store.onIntent(CalculatorIntent.Evaluate)
        testScheduler.advanceUntilIdle()
        store.onIntent(CalculatorIntent.Clear)
        testScheduler.advanceUntilIdle()

        assertEquals("", store.state.value.expression)
        assertEquals(1, store.state.value.history.size)
    }
}
