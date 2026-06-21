/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.ui.di

import com.vela.apps.draw.data.drawPlatformModule
import com.vela.apps.draw.presentation.DrawStore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole Draw app, including the platform exporter. */
val drawModule = module {
    includes(drawPlatformModule())
    viewModelOf(::DrawStore)
}
