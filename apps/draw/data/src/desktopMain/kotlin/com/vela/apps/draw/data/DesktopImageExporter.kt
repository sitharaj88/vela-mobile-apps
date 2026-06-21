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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import java.util.Locale
import javax.imageio.ImageIO

/**
 * Renders the portable drawing to a [BufferedImage] and writes a PNG into the user's `Pictures`
 * directory (falling back to the home directory). This is a real, fully-working export on Desktop.
 */
internal class DesktopImageExporter : ImageExporter {

    override suspend fun exportPng(drawing: Drawing, fileName: String): ExportResult =
        withContext(Dispatchers.IO) {
            val image = BufferedImage(drawing.width, drawing.height, BufferedImage.TYPE_INT_ARGB)
            val graphics = image.createGraphics()
            try {
                paint(graphics, drawing)
            } finally {
                graphics.dispose()
            }
            val target = uniqueFile(fileName)
            target.parentFile?.mkdirs()
            ImageIO.write(image, "png", target)
            ExportResult(target.absolutePath)
        }

    private fun paint(graphics: Graphics2D, drawing: Drawing) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.color = awtColor(drawing.backgroundArgb)
        graphics.fillRect(0, 0, drawing.width, drawing.height)
        renderCommandsOf(drawing.elements).forEach { command -> draw(graphics, command) }
    }

    private fun draw(graphics: Graphics2D, command: RenderCommand) {
        graphics.color = awtColor(command.colorArgb)
        graphics.stroke = BasicStroke(command.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        when (command) {
            is RenderCommand.Dot -> {
                val r = command.width / 2.0
                val d = command.width.toDouble()
                graphics.fill(Ellipse2D.Double(command.cx - r, command.cy - r, d, d))
            }
            is RenderCommand.PolyLine -> {
                val path = GeneralPath()
                path.moveTo(command.points.first().x.toDouble(), command.points.first().y.toDouble())
                command.points.drop(1).forEach { path.lineTo(it.x.toDouble(), it.y.toDouble()) }
                graphics.draw(path)
            }
            is RenderCommand.Line ->
                graphics.draw(
                    Line2D.Double(
                        command.x1.toDouble(), command.y1.toDouble(), command.x2.toDouble(), command.y2.toDouble(),
                    ),
                )
            is RenderCommand.Rect ->
                graphics.draw(
                    Rectangle2D.Double(
                        command.left.toDouble(), command.top.toDouble(),
                        (command.right - command.left).toDouble(), (command.bottom - command.top).toDouble(),
                    ),
                )
            is RenderCommand.Ellipse ->
                graphics.draw(
                    Ellipse2D.Double(
                        command.left.toDouble(), command.top.toDouble(),
                        (command.right - command.left).toDouble(), (command.bottom - command.top).toDouble(),
                    ),
                )
        }
    }

    private fun awtColor(argb: Long): Color {
        val value = argb.toInt()
        val a = (value ushr ALPHA_SHIFT) and BYTE_MASK
        val r = (value ushr RED_SHIFT) and BYTE_MASK
        val g = (value ushr GREEN_SHIFT) and BYTE_MASK
        val b = value and BYTE_MASK
        return Color(r, g, b, a)
    }

    private fun uniqueFile(fileName: String): File {
        val pictures = File(System.getProperty("user.home"), "Pictures")
        val dir = if (pictures.isDirectory || pictures.mkdirs()) pictures else File(System.getProperty("user.home"))
        val stamp = System.currentTimeMillis()
        return File(dir, String.format(Locale.ROOT, "%s-%d.png", fileName, stamp))
    }

    private companion object {
        const val BYTE_MASK = 0xFF
        const val ALPHA_SHIFT = 24
        const val RED_SHIFT = 16
        const val GREEN_SHIFT = 8
    }
}
