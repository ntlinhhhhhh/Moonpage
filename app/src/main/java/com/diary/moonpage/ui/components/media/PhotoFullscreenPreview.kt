package com.diary.moonpage.ui.components.media

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diary.moonpage.R
import java.io.File

@Composable
fun PhotoFullscreenPreview(
    imageUrl: String,
    localPath: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dismissThreshold = remember { with(density) { 150.dp.toPx() } }
    
    val imageData = remember(localPath, imageUrl) {
        if (localPath != null && File(localPath).exists()) File(localPath) else imageUrl
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val swipeOffsetY = remember { Animatable(0f) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val backgroundAlpha = (1f - (swipeOffsetY.value / 600f)).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            },
        color = backgroundColor.copy(alpha = backgroundAlpha)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })

                        if (swipeOffsetY.value > dismissThreshold) {
                            onDismiss()
                        } else {
                            scope.launch {
                                swipeOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = remember(imageData) {
                    ImageRequest.Builder(context)
                        .data(imageData)
                        .crossfade(true)
                        .build()
                },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y + swipeOffsetY.value
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val nextScale = (scale * zoom).coerceIn(1f, 5f)
                            if (nextScale > 1f || scale > 1f) {
                                scale = nextScale
                                offset += pan
                            } else {
                                // scale is 1f
                                if (pan.y > 0 || swipeOffsetY.value > 0) {
                                    scope.launch {
                                        swipeOffsetY.snapTo((swipeOffsetY.value + pan.y).coerceAtLeast(0f))
                                    }
                                }
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
