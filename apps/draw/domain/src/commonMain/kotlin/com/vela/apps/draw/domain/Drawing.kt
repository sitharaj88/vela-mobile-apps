/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.domain

/**
 * A self-contained, framework-free snapshot of a drawing: its pixel size, background color and the
 * ordered [elements] to paint on top. This is the single portable model every platform's
 * [ImageExporter] renders, so export logic stays shared and the platform code only writes bytes.
 */
data class Drawing(
    val width: Int,
    val height: Int,
    val backgroundArgb: Long,
    val elements: List<DrawElement>,
)
