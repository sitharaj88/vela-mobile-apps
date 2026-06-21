/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.draw.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.vela.apps.draw.domain.Drawing
import com.vela.apps.draw.domain.ExportResult
import com.vela.apps.draw.domain.ImageExporter
import com.vela.apps.draw.domain.RenderCommand
import com.vela.apps.draw.domain.renderCommandsOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Rasterizes the portable drawing onto an [android.graphics.Bitmap], inserts the PNG into the
 * shared `Pictures/Vela` collection via [MediaStore], then launches a share chooser. Real export.
 */
internal class AndroidImageExporter(private val context: Context) : ImageExporter {

    override suspend fun exportPng(drawing: Drawing, fileName: String): ExportResult =
        withContext(Dispatchers.IO) {
            val bitmap = renderBitmap(drawing)
            val uri = saveToMediaStore(bitmap, fileName)
                ?: error("MediaStore rejected the image insert")
            bitmap.recycle()
            shareImage(uri)
            ExportResult(uri.toString())
        }

    private fun renderBitmap(drawing: Drawing): Bitmap {
        val bitmap = Bitmap.createBitmap(drawing.width, drawing.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(drawing.backgroundArgb.toInt())
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        renderCommandsOf(drawing.elements).forEach { command -> draw(canvas, command, fill, stroke) }
        return bitmap
    }

    private fun draw(canvas: Canvas, command: RenderCommand, fill: Paint, stroke: Paint) {
        fill.color = command.colorArgb.toInt()
        stroke.color = command.colorArgb.toInt()
        stroke.strokeWidth = command.width
        when (command) {
            is RenderCommand.Dot -> canvas.drawCircle(command.cx, command.cy, command.width / 2f, fill)
            is RenderCommand.PolyLine -> {
                val path = Path()
                path.moveTo(command.points.first().x, command.points.first().y)
                command.points.drop(1).forEach { path.lineTo(it.x, it.y) }
                canvas.drawPath(path, stroke)
            }
            is RenderCommand.Line -> canvas.drawLine(command.x1, command.y1, command.x2, command.y2, stroke)
            is RenderCommand.Rect ->
                canvas.drawRect(command.left, command.top, command.right, command.bottom, stroke)
            is RenderCommand.Ellipse ->
                canvas.drawOval(command.left, command.top, command.right, command.bottom, stroke)
        }
    }

    private fun saveToMediaStore(bitmap: Bitmap, fileName: String): Uri? {
        val name = "$fileName-${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Vela")
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)
        } ?: return null
        return uri
    }

    private fun shareImage(uri: Uri) {
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(share, "Share drawing").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(chooser) }
    }

    private companion object {
        const val PNG_QUALITY = 100
    }
}
