package com.diary.moonpage.presentation.components.moment

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.diary.moonpage.domain.model.Moment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MomentFeedItem(
    moment: Moment, 
    localPath: String? = null,
    avatarUrl: String? = null,
    localAvatarPath: String? = null,
    onImageClick: () -> Unit = {}
) {
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current
    
    val imageData = remember(localPath, moment.imageUrl) {
        if (localPath != null && File(localPath).exists()) File(localPath) else moment.imageUrl
    }

    val isLocalFile = imageData is File

    // Tối ưu ImageRequest cho chất lượng cao và tốc độ load
    val imageRequest = remember(imageData) {
        ImageRequest.Builder(context)
            .data(imageData)
            .size(Size.ORIGINAL) // Giữ chất lượng gốc cho feed full screen
            .scale(Scale.FILL)
            .precision(Precision.INEXACT)
            .crossfade(300) // Tăng nhẹ crossfade cho mượt
            .memoryCacheKey("feed_${imageData}")
            .diskCachePolicy(if (isLocalFile) CachePolicy.DISABLED else CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    var isLoaded by remember { mutableStateOf(false) }

    val shimmerAlpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp).height(56.dp))
        Spacer(modifier = Modifier.height(60.dp))

        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(if (isLoaded) Color.Transparent else onBgColor.copy(alpha = shimmerAlpha))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3f)
                        if (scale > 1f) {
                            offset += pan
                        } else {
                            offset = androidx.compose.ui.geometry.Offset.Zero
                        }
                    }
                }
                .clickable { 
                    if (scale == 1f) onImageClick() 
                    else {
                        scale = 1f
                        offset = androidx.compose.ui.geometry.Offset.Zero
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Crop,
                onSuccess = { isLoaded = true }
            )
            
            val inferredTag = remember(moment) {
                when {
                    moment.rating != null && moment.rating > 0 -> MomentTag("review", Icons.Rounded.Star, "Review", Color.White, Color.Yellow.copy(0.6f))
                    moment.caption?.startsWith("Rating: ") == true -> MomentTag("review", Icons.Rounded.Star, "Review", Color.White, Color.Yellow.copy(0.6f))
                    moment.location != null -> MomentTag("location", Icons.Rounded.LocationOn, "Location", Color.White, Color.Blue.copy(0.6f))
                    moment.weather != null -> MomentTag("weather", Icons.Rounded.Cloud, "Weather", Color.White, Color.Cyan.copy(0.6f))
                    moment.caption in listOf("Sunny ☀️", "Cloudy ☁️", "Rainy 🌧️", "Snowy ❄️", "Windy 💨") -> MomentTag("weather", Icons.Rounded.Cloud, "Weather", Color.White, Color.Cyan.copy(0.6f))
                    moment.caption == "Party Time!" -> MomentTag("party", null, "Party Time!", containerColor = Color(0xFF80FFE8), contentColor = Color.Black)
                    moment.caption == "OOTD" -> MomentTag("ootd", null, "OOTD", containerColor = Color.White, contentColor = Color.Black)
                    moment.caption == "Miss you" -> MomentTag("missyou", null, "Miss you", containerColor = Color(0xFFFF4B4B), contentColor = Color.White)
                    else -> MomentTag("text", null, "Message", Color.White, Color.Black.copy(0.75f))
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(bottom = 4.dp), contentAlignment = Alignment.BottomCenter) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        val textToShow = when (inferredTag.id) {
                            "review" -> ""
                            "location" -> moment.location ?: moment.caption ?: ""
                            "weather" -> moment.weather ?: moment.caption ?: ""
                            else -> moment.caption ?: ""
                        }
                        
                        val shouldShow = inferredTag.id == "review" || textToShow.isNotEmpty() || inferredTag.icon != null

                        if (shouldShow) {
                            Surface(
                                color = inferredTag.containerColor ?: Color.Black.copy(alpha = 0.75f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (inferredTag.id != "review") {
                                        inferredTag.icon?.let { 
                                            Icon(it, null, tint = inferredTag.contentColor, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                    }

                                    if (inferredTag.id == "review") {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val parsedRating = if (moment.caption?.startsWith("Rating: ") == true) {
                                                moment.caption.removePrefix("Rating: ").removeSuffix(" stars").toFloatOrNull() ?: 0f
                                            } else 0f
                                            val finalRating = if (moment.rating != null && moment.rating > 0) moment.rating else parsedRating

                                            repeat(5) { index ->
                                                val starIndex = index + 1
                                                val starIcon = when {
                                                    finalRating >= starIndex -> Icons.Default.Star
                                                    finalRating >= starIndex - 0.5f -> Icons.AutoMirrored.Filled.StarHalf
                                                    else -> Icons.Default.StarBorder
                                                }
                                                Icon(
                                                    imageVector = starIcon,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFD700),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        if (textToShow.isNotEmpty()) {
                                            Text(
                                                text = textToShow,
                                                color = inferredTag.contentColor,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 16.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TagChip(
            tag = MomentTag(
                id = "time",
                icon = Icons.Rounded.AccessTime,
                text = formatShortTime(moment.capturedAt),
                containerColor = onBgColor.copy(alpha = 0.1f),
                contentColor = onBgColor.copy(alpha = 0.6f)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(onBgColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = localAvatarPath ?: avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Me",
                color = onBgColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatRelativeTime(moment.capturedAt),
                color = onBgColor.copy(alpha = 0.5f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatShortTime(dateString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = sdf.parse(dateString) ?: return ""
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        ""
    }
}

private fun formatRelativeTime(dateString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = sdf.parse(dateString) ?: return ""
        val now = Date()
        val diffMillis = now.time - date.time
        
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        when {
            minutes < 1 -> "now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            else -> "${days}d"
        }
    } catch (e: Exception) {
        ""
    }
}
