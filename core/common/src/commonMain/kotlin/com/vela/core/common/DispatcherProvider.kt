/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Injectable dispatchers so coroutines are testable (swap for a test dispatcher in unit tests).
 * `io` maps to a platform IO pool where one exists; on platforms without it, falls back to Default.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
}

/** Default production dispatchers. `io` is provided per platform via [platformIoDispatcher]. */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val io: CoroutineDispatcher = platformIoDispatcher()
}

/** IO dispatcher resolution differs per platform (JVM/Android have a real IO pool; native does not). */
expect fun platformIoDispatcher(): CoroutineDispatcher
