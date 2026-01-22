package com.osia.petsos.ui.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
        val pagerState = rememberPagerState(
            initialPage = initialIndex,
            pageCount = { images.size }
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
                val imageUrl = images[page]
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
            
            // Page Indicator (Optional but helpful)
             if (images.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                     androidx.compose.material3.Text(
                        text = "${pagerState.currentPage + 1} / ${images.size}",
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

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 3f)
        
        // Only allow pan (offset) if zoomed in
        if (scale > 1f) {
             val maxX = (scale - 1) * 1000 // Approximate bound, ideally calculated from container size
             val maxY = (scale - 1) * 1000
             
             val newOffset = offset + offsetChange
             offset = Offset(
                 newOffset.x.coerceIn(-maxX, maxX),
                 newOffset.y.coerceIn(-maxY, maxY)
             )
        } else {
            offset = Offset.Zero
        }
    }

    // Reset zoom when image changes or is swiped away? 
    // HorizontalPager handles disposal so this state is per page instance.
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = state)
            // Handle double tap to zoom reset/max
             .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                     scale = (scale * zoom).coerceIn(1f, 3f)
                     if (scale > 1f) {
                         val newOffset = offset + pan
                         offset = newOffset
                     } else {
                         offset = Offset.Zero
                     }
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
