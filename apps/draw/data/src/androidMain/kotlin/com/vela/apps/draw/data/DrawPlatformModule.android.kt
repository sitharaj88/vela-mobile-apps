/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.data

import com.vela.apps.draw.domain.ImageExporter
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android renders to an [android.graphics.Bitmap] and saves through `MediaStore`, then shares it. */
actual fun drawPlatformModule(): Module = module {
    single<ImageExporter> { AndroidImageExporter(androidContext()) }
}
