/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vela.apps.gallery.domain.MediaItem
import kotlinx.coroutines.delay

private const val MAX_ZOOM = 5f
private const val MIN_ZOOM = 1f
private const val SLIDESHOW_DELAY_MS = 3000L

/**
 * Full-screen pager viewer with pinch-zoom + pan per page, a favorite toggle, and a slideshow mode
 * that auto-advances pages until stopped.
 */
@Composable
internal fun MediaViewer(
    items: List<MediaItem>,
    initialIndex: Int,
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { items.size }
    var slideshow by remember { mutableStateOf(false) }

    LaunchedEffect(slideshow) {
        while (slideshow) {
            delay(SLIDESHOW_DELAY_MS)
            val next = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(next)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            ZoomableMedia(items[page])
        }
        ViewerControls(
            item = items.getOrNull(pagerState.currentPage),
            isFavorite = items.getOrNull(pagerState.currentPage)?.id in favorites,
            slideshow = slideshow,
            onToggleFavorite = onToggleFavorite,
            onToggleSlideshow = { slideshow = !slideshow },
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun ViewerControls(
    item: MediaItem?,
    isFavorite: Boolean,
    slideshow: Boolean,
    onToggleFavorite: (String) -> Unit,
    onToggleSlideshow: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
        }
        Box(Modifier.weight(1f))
        IconButton(onClick = onToggleSlideshow) {
            Icon(
                imageVector = if (slideshow) Icons.Filled.Stop else Icons.Filled.Slideshow,
                contentDescription = "Slideshow",
                tint = Color.White,
            )
        }
        if (item != null) {
            IconButton(onClick = { onToggleFavorite(item.id) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ZoomableMedia(item: MediaItem) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(item.id) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                    if (scale > MIN_ZOOM) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY,
                ),
        )
        if (item.isVideo) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier.size(72.dp),
            )
        }
    }
}
