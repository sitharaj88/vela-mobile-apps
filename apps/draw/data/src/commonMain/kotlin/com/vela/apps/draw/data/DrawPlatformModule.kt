/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.data

import org.koin.core.module.Module

/**
 * Binds [com.vela.apps.draw.domain.ImageExporter] per platform. The actuals differ only in how they
 * rasterize the portable drawing and where they write the PNG: Desktop uses `ImageIO`, Android uses
 * `MediaStore` + a share intent, iOS uses `UIImage` / Photos (best-effort).
 */
expect fun drawPlatformModule(): Module
