/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.data

import com.vela.apps.draw.domain.ImageExporter
import org.koin.core.module.Module
import org.koin.dsl.module

/** Desktop renders to a [java.awt.image.BufferedImage] and writes PNG via `ImageIO`. */
actual fun drawPlatformModule(): Module = module {
    single<ImageExporter> { DesktopImageExporter() }
}
