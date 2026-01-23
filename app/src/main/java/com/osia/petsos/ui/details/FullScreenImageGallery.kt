package com.osia.petsos.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.osia.petsos.core.config.FirebaseConfig

@Composable
fun FullScreenImageGallery(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Full screen
            dismissOnBackPress = true
        )
    ) {
        // Infinite scroll logic
        val pageCount = if (images.size > 1) Int.MAX_VALUE else images.size
        // Start in the middle so user can scroll left immediately
        val initialPage = if (images.size > 1) {
            val middle = Int.MAX_VALUE / 2
            // Adjust middle to align with index 0, then add initialIndex
            middle - (middle % images.size) + initialIndex
        } else {
            initialIndex
        }

        val pagerState = rememberPagerState(
            initialPage = initialPage,
            pageCount = { pageCount }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val actualIndex = page % images.size
                val imageUrl = images[actualIndex]
                ZoomableImage(imageUrl = imageUrl)
            }

            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            
            // Page Indicator
             if (images.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val currentActualPage = (pagerState.currentPage % images.size) + 1
                     Text(
                        text = "$currentActualPage / ${images.size}",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(imageUrl: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2f
                        offset = Offset.Zero
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()
                        
                        // If scale is 1, only listen to multitouch (zoom)
                        // If scale > 1, listen to everything (pan + zoom)
                        val isZooming = event.changes.size > 1
                        val shouldHandle = scale > 1f || isZooming
                        
                        if (shouldHandle) {
                             if (zoomChange != 1f || panChange != Offset.Zero) {
                                 scale = (scale * zoomChange).coerceIn(1f, 3f)
                                 
                                 // Adjust offset
                                 val newOffset = offset + panChange
                                 val maxX = (scale - 1) * 1000 // Approximate bound
                                 val maxY = (scale - 1) * 1000
                                 
                                 offset = Offset(
                                     newOffset.x.coerceIn(-maxX, maxX),
                                     newOffset.y.coerceIn(-maxY, maxY)
                                 )
                                 
                                 event.changes.forEach { 
                                     if (it.positionChanged()) {
                                         it.consume() 
                                     }
                                 }
                             }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
         AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(FirebaseConfig.getStorageUrl(imageUrl))
                .crossfade(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = "Full Screen Image",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}
