/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base class for all Vela presentation stores (MVI / unidirectional data flow).
 *
 * - [state] is the single immutable source of truth, rendered by Compose.
 * - [effects] are one-shot events (navigation, snackbars) — consumed exactly once.
 * - Screens send [Intent]s via [onIntent]; subclasses reduce them with [setState] and trigger
 *   side effects with [emitEffect].
 *
 * Hosted in a multiplatform [ViewModel] so it survives configuration changes on Android and has a
 * managed [viewModelScope] on every platform.
 */
abstract class MviStore<S, I, E>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects: Flow<E> = _effects.receiveAsFlow()

    /** Current state snapshot — convenient inside reducers. */
    protected val currentState: S get() = _state.value

    /** Entry point for every user action. Implement the reduce/side-effect logic here. */
    abstract fun onIntent(intent: I)

    protected fun setState(reducer: S.() -> S) = _state.update(reducer)

    protected fun emitEffect(effect: E) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
