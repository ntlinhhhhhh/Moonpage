package com.diary.moonpage.presentation.screens.moment

import android.net.Uri
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.presentation.components.moment.MomentTag
import com.diary.moonpage.presentation.theme.nunitoFontFamily
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentUploadScreen(
    capturedImageUri: Uri,
    capturedLensFacing: Int,
    pagerState: PagerState,
    allTags: List<MomentTag>,
    userMessage: String,
    onUserMessageChange: (String) -> Unit,
    userRating: Float,
    onUserRatingChange: (Float) -> Unit,
    userLocation: String,
    onLocationClick: () -> Unit,
    userWeather: String,
    onWeatherClick: () -> Unit,
    isLoading: Boolean,
    isSuccess: Boolean,
    onCancel: () -> Unit,
    onUpload: (File, String) -> Unit,
    onShowTagSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val onBgColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(pagerState.currentPage) {
        val currentTag = allTags[pagerState.currentPage]
        if (currentTag.id == "text") {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            focusManager.clearFocus()
            keyboardController?.hide()
            if (currentTag.id == "location") onLocationClick()
            if (currentTag.id == "weather") onWeatherClick()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(56.dp)) {
            Text(
                text = "Upload",
                color = onBgColor,
                fontSize = 22.sp,
                fontFamily = nunitoFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 4f)
            val maxOffset = 500f * (scale - 1f)
            offsetX = (offsetX + panChange.x).coerceIn(-maxOffset, maxOffset)
            offsetY = (offsetY + panChange.y).coerceIn(-maxOffset, maxOffset)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {
                        scale = 1f; offsetX = 0f; offsetY = 0f
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = capturedImageUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale * if (capturedLensFacing == CameraSelector.LENS_FACING_FRONT) -1f else 1f,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .transformable(state = transformState),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .clickable { onShowTagSheet() },
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            try {
                                val inputStream = context.contentResolver.openInputStream(capturedImageUri)
                                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                if (bitmap != null) {
                                    ImageUtils.saveBitmapToGallery(context, bitmap)
                                    Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Rounded.FileDownload,
                        null, tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp))
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(bottom = 4.dp), contentAlignment = Alignment.BottomCenter) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    contentPadding = PaddingValues(horizontal = 48.dp)
                ) { page ->
                    val tag = allTags[page]
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Surface(
                            color = tag.containerColor ?: Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (tag.id != "review") {
                                    tag.icon?.let { Icon(it, null, tint = tag.contentColor, modifier = Modifier.size(18.dp).clickable { onShowTagSheet() }) ; Spacer(modifier = Modifier.width(8.dp)) }
                                }
                                when(tag.id) {
                                    "text" -> {
                                        BasicTextField(
                                            value = userMessage,
                                            onValueChange = { if (!it.contains("\n")) onUserMessageChange(it) },
                                            modifier = Modifier.focusRequester(focusRequester),
                                            textStyle = TextStyle(color = tag.contentColor, fontWeight = FontWeight.Medium, fontSize = 16.sp, textAlign = TextAlign.Center),
                                            cursorBrush = SolidColor(tag.contentColor),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                            keyboardActions = KeyboardActions(onDone = {
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                            }),
                                            decorationBox = { innerTextField ->
                                                if (userMessage.isEmpty()) Text("Add a message", color = tag.contentColor.copy(alpha = 0.6f), fontSize = 16.sp)
                                                innerTextField()
                                            }
                                        )
                                    }
                                    "review" -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            repeat(5) { index ->
                                                val starIndex = index + 1
                                                val starIcon = when {
                                                    userRating >= starIndex -> Icons.Default.Star
                                                    userRating >= starIndex - 0.5f -> Icons.AutoMirrored.Filled.StarHalf
                                                    else -> Icons.Default.StarBorder
                                                }
                                                Icon(
                                                    imageVector = starIcon,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFD700),
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clickable {
                                                            val newRating = if (userRating == starIndex.toFloat()) starIndex - 0.5f 
                                                                           else if (userRating == starIndex - 0.5f) starIndex - 1.0f
                                                                           else starIndex.toFloat()
                                                            onUserRatingChange(newRating)
                                                        }
                                                )
                                            }
                                        }
                                    }
                                    "location" -> {
                                        Text(text = if (userLocation.isNotEmpty()) userLocation else "Tap to add location", color = tag.contentColor, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onLocationClick() })
                                    }
                                    "weather" -> {
                                        Text(text = userWeather, color = tag.contentColor, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onWeatherClick() })
                                    }
                                    else -> Text(text = tag.text, color = tag.contentColor, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
        }

        Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(allTags.size) { i ->
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (i == pagerState.currentPage) onBgColor else onBgColor.copy(alpha = 0.3f)))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onCancel() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }

            Box(
                modifier = Modifier.size(80.dp).border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape).padding(8.dp).clip(CircleShape).background(Color.Transparent).clickable(enabled = !isLoading && !isSuccess) {
                    scope.launch {
                        val currentTag = allTags[pagerState.currentPage]
                        val caption = when(currentTag.id) {
                            "text" -> userMessage
                            "review" -> "Rating: $userRating stars"
                            "location" -> userLocation
                            "weather" -> userWeather
                            else -> currentTag.text
                        }
                        
                        val compressedFile = ImageUtils.compressAndCropSquare(
                            context = context,
                            uri = capturedImageUri,
                            lensFacing = capturedLensFacing
                        )
                        
                        if (compressedFile != null) {
                            onUpload(compressedFile, caption)
                        } else {
                            Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isSuccess,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "Success"
                ) { success ->
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(if (success) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (success) Icons.Default.Check else Icons.AutoMirrored.Rounded.Send, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
                    }
                }
            }

            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onShowTagSheet() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}
