/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.domain

/**
 * Framework-free abstraction over a device torch / flash unit.
 *
 * Implementations live in the `data` module's platform source sets. [isAvailable] reflects whether
 * the current device actually has a controllable flash; on platforms without one (or without
 * permission) it is `false` and [setEnabled] is a no-op.
 */
interface Torch {
    /** Whether this device exposes a controllable flash unit. */
    val isAvailable: Boolean

    /** Turn the torch on ([on] = true) or off. No-op when [isAvailable] is false. */
    fun setEnabled(on: Boolean)
}
