package com.diary.moonpage.ui.screens.moment.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.FlashAuto
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlipCameraIos
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.diary.moonpage.ui.screens.tutorial.tutorialTarget
import com.diary.moonpage.ui.screens.tutorial.TutorialStep
import coil.compose.AsyncImage
import java.io.File
import java.util.concurrent.Executor

@Composable
fun CameraMainUI(
    onSelectFromGallery: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onImageCaptured: (Uri, Int) -> Unit,
    avatarUrl: String? = null,
    onAvatarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var currentZoomRatio by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(lensFacing, lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                } catch (e: Exception) {
                    Log.e("CameraMainUI", "Camera binding failed", e)
                }
        }, mainExecutor)
    }

    // Explicit cleanup when Composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    cameraProviderFuture.get().unbindAll()
                } catch (e: Exception) {
                    Log.e("CameraMainUI", "Error unbinding on dispose", e)
                }
            }, mainExecutor)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Box(modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .align(Alignment.CenterStart)
                    .clickable { onAvatarClick() }
            ) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Flash Toggle (Only for Back Camera)
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .align(Alignment.CenterEnd)
                        .clickable {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                                else -> ImageCapture.FLASH_MODE_OFF
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val flashIcon = when (flashMode) {
                        ImageCapture.FLASH_MODE_ON -> Icons.Rounded.FlashOn
                        ImageCapture.FLASH_MODE_AUTO -> Icons.Rounded.FlashAuto
                        else -> Icons.Rounded.FlashOff
                    }
                    Icon(flashIcon, "Flash", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        // Camera Preview with pinch-to-zoom
        val zoomTransformState = rememberTransformableState { zoomChange, _, _ ->
            val maxRatio = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 4f
            currentZoomRatio = (currentZoomRatio * zoomChange).coerceIn(1f, maxRatio)
            camera?.cameraControl?.setZoomRatio(currentZoomRatio)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black)
                .transformable(state = zoomTransformState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val factory = previewView.meteringPointFactory
                            val point = factory.createPoint(offset.x, offset.y)
                            val action = FocusMeteringAction.Builder(point).build()
                            camera?.cameraControl?.startFocusAndMetering(action)
                        },
                        onDoubleTap = {
                            currentZoomRatio = 1f
                            camera?.cameraControl?.setZoomRatio(1f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { onSelectFromGallery() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }

            // Capture Button
            CaptureButton(
                onClick = {
                    takePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        flashMode = flashMode,
                        executor = mainExecutor,
                        onImageCaptured = { uri -> onImageCaptured(uri, lensFacing) },
                        onError = { Log.e("CameraMainUI", "Capture failed", it) }
                    )
                },
                modifier = Modifier.tutorialTarget(TutorialStep.HighlightCameraCapture)
            )

            // Flip Camera Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) 
                            CameraSelector.LENS_FACING_FRONT 
                        else 
                            CameraSelector.LENS_FACING_BACK
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.FlipCameraIos, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // History Button
        Row(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onNavigateToHistory() }
                .padding(8.dp)
                .tutorialTarget(TutorialStep.HighlightMomentHistoryButton),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.History, "History", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("HISTORY", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    flashMode: Int,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    imageCapture.flashMode = flashMode
    val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            onImageCaptured(Uri.fromFile(file))
        }
        override fun onError(exception: ImageCaptureException) {
            onError(exception)
        }
    })
}
