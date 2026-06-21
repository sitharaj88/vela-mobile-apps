/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.data

import com.vela.apps.draw.domain.Drawing
import com.vela.apps.draw.domain.ExportResult
import com.vela.apps.draw.domain.ImageExporter
import com.vela.apps.draw.domain.RenderCommand
import com.vela.apps.draw.domain.renderCommandsOf
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGContextAddEllipseInRect
import platform.CoreGraphics.CGContextAddLineToPoint
import platform.CoreGraphics.CGContextBeginPath
import platform.CoreGraphics.CGContextFillEllipseInRect
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextMoveToPoint
import platform.CoreGraphics.CGContextSetFillColorWithColor
import platform.CoreGraphics.CGContextSetLineCap
import platform.CoreGraphics.CGContextSetLineJoin
import platform.CoreGraphics.CGContextSetLineWidth
import platform.CoreGraphics.CGContextSetStrokeColorWithColor
import platform.CoreGraphics.CGContextStrokePath
import platform.CoreGraphics.CGContextStrokeRect
import platform.CoreGraphics.CGLineCap
import platform.CoreGraphics.CGLineJoin
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS renders to a [UIImage] and writes it to the Photos album (best-effort). */
actual fun drawPlatformModule(): Module = module {
    single<ImageExporter> { IosImageExporter() }
}

/**
 * Best-effort iOS exporter: rasterizes the drawing into a UIKit image context and saves the result
 * to the Photos album. Saving requires the `NSPhotoLibraryAddUsageDescription` entitlement.
 *
 * TODO(ios): present a `UIActivityViewController` share sheet (needs the key window's root view
 * controller) and observe the `UIImageWriteToSavedPhotosAlbum` completion selector to surface real
 * success/failure instead of optimistically reporting success. cinterop selectors are unverified.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosImageExporter : ImageExporter {

    override suspend fun exportPng(drawing: Drawing, fileName: String): ExportResult {
        val size = CGSizeMake(drawing.width.toDouble(), drawing.height.toDouble())
        UIGraphicsBeginImageContextWithOptions(size, opaque = true, scale = 1.0)
        try {
            paintBackground(drawing)
            renderCommandsOf(drawing.elements).forEach { command -> draw(command) }
            val image = UIGraphicsGetImageFromCurrentImageContext()
            if (image != null) UIImageWriteToSavedPhotosAlbum(image, null, null, null)
        } finally {
            UIGraphicsEndImageContext()
        }
        return ExportResult("Photos")
    }

    private fun paintBackground(drawing: Drawing) {
        val context = UIGraphicsGetCurrentContext()
        CGContextSetFillColorWithColor(context, uiColor(drawing.backgroundArgb).CGColor)
        CGContextFillRect(context, CGRectMake(0.0, 0.0, drawing.width.toDouble(), drawing.height.toDouble()))
    }

    private fun draw(command: RenderCommand) {
        val context = UIGraphicsGetCurrentContext()
        val color = uiColor(command.colorArgb)
        CGContextSetStrokeColorWithColor(context, color.CGColor)
        CGContextSetFillColorWithColor(context, color.CGColor)
        CGContextSetLineWidth(context, command.width.toDouble())
        CGContextSetLineCap(context, CGLineCap.kCGLineCapRound)
        CGContextSetLineJoin(context, CGLineJoin.kCGLineJoinRound)
        when (command) {
            is RenderCommand.Dot -> {
                val r = command.width.toDouble() / 2.0
                val d = command.width.toDouble()
                CGContextFillEllipseInRect(context, CGRectMake(command.cx - r, command.cy - r, d, d))
            }
            is RenderCommand.PolyLine -> {
                CGContextBeginPath(context)
                CGContextMoveToPoint(context, command.points.first().x.toDouble(), command.points.first().y.toDouble())
                command.points.drop(1).forEach { CGContextAddLineToPoint(context, it.x.toDouble(), it.y.toDouble()) }
                CGContextStrokePath(context)
            }
            is RenderCommand.Line -> {
                CGContextBeginPath(context)
                CGContextMoveToPoint(context, command.x1.toDouble(), command.y1.toDouble())
                CGContextAddLineToPoint(context, command.x2.toDouble(), command.y2.toDouble())
                CGContextStrokePath(context)
            }
            is RenderCommand.Rect ->
                CGContextStrokeRect(context, rectOf(command.left, command.top, command.right, command.bottom))
            is RenderCommand.Ellipse -> {
                CGContextBeginPath(context)
                CGContextAddEllipseInRect(context, rectOf(command.left, command.top, command.right, command.bottom))
                CGContextStrokePath(context)
            }
        }
    }

    private fun rectOf(left: Float, top: Float, right: Float, bottom: Float) =
        CGRectMake(left.toDouble(), top.toDouble(), (right - left).toDouble(), (bottom - top).toDouble())

    private fun uiColor(argb: Long): UIColor {
        val value = argb.toInt()
        val a = ((value ushr ALPHA_SHIFT) and BYTE_MASK) / MAX_CHANNEL
        val r = ((value ushr RED_SHIFT) and BYTE_MASK) / MAX_CHANNEL
        val g = ((value ushr GREEN_SHIFT) and BYTE_MASK) / MAX_CHANNEL
        val b = (value and BYTE_MASK) / MAX_CHANNEL
        return UIColor(red = r, green = g, blue = b, alpha = a)
    }

    private companion object {
        const val BYTE_MASK = 0xFF
        const val ALPHA_SHIFT = 24
        const val RED_SHIFT = 16
        const val GREEN_SHIFT = 8
        const val MAX_CHANNEL = 255.0
    }
}
