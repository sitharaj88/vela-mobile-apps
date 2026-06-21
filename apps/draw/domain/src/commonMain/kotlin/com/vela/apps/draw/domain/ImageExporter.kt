/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.domain

/** Where an exported image ended up, surfaced to the UI for a confirmation message. */
data class ExportResult(val location: String)

/**
 * Renders a portable [Drawing] to a PNG image and persists/shares it. Each platform provides a real
 * actual: Desktop writes to the Pictures folder via `ImageIO`, Android saves through `MediaStore`
 * and offers a share sheet, iOS writes to Photos / a share sheet (best-effort).
 */
interface ImageExporter {
    /** Renders [drawing] to PNG and saves it, returning where it landed, or throws on failure. */
    suspend fun exportPng(drawing: Drawing, fileName: String): ExportResult
}
