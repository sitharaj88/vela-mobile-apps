/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.ui.di

import com.vela.apps.flashlight.data.flashlightPlatformModule
import com.vela.apps.flashlight.presentation.FlashlightStore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole Flashlight app. */
val flashlightModule = module {
    includes(flashlightPlatformModule())
    viewModelOf(::FlashlightStore)
}
