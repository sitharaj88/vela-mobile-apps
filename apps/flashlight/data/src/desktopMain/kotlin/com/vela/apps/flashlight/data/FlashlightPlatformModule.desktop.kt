/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.data

import com.vela.apps.flashlight.domain.Torch
import org.koin.core.module.Module
import org.koin.dsl.module

/** Desktop has no flash unit — degrade gracefully with an unavailable no-op torch. */
private object DesktopTorch : Torch {
    override val isAvailable: Boolean = false
    override fun setEnabled(on: Boolean) = Unit
}

actual fun flashlightPlatformModule(): Module = module {
    single<Torch> { DesktopTorch }
}
