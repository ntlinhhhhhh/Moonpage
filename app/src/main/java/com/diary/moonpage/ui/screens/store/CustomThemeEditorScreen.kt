package com.diary.moonpage.ui.screens.store

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
 import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diary.moonpage.R
import com.diary.moonpage.ui.components.feedback.GlobalSnackbarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ==========================================
// ROUTE ENTRY POINT
// ==========================================

@Composable
fun CustomThemeEditorRoute(
    onNavigateBack: () -> Unit,
    viewModel: CustomThemeEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CustomThemeEditorEffect.Saved -> onNavigateBack()
                is CustomThemeEditorEffect.Error -> GlobalSnackbarManager.show(effect.message)
            }
        }
    }

    CustomThemeEditorRoot(
        uiState = uiState,
        onNameChange = viewModel::updateName,
        onImagePicked = viewModel::setBackgroundUri,
        onApplyEditedImage = viewModel::applyPendingBackground,
        onCancelEditedImage = viewModel::cancelPendingBackground,
        onSolidBackgroundSelected = viewModel::setSolidBackgroundColor,
        onGradientStartSelected = viewModel::setGradientBackgroundStartColor,
        onGradientEndSelected = viewModel::setGradientBackgroundEndColor,
        onPrimaryFocus = viewModel::focusPrimaryColor,
        onFocusedColorSelected = viewModel::applyFocusedColor,
        onIconSelected = viewModel::selectIcon,
        onBrushColorSelected = viewModel::setBrushColor,
        onBrushSizeChange = viewModel::setBrushSize,
        onBrushTypeSelected = viewModel::setBrushType,
        onEraserChanged = viewModel::setEraser,
        onSetScreen = viewModel::setScreen,
        onSetEditMode = viewModel::setEditMode,
        onSetGradientNode = viewModel::setGradientNode,
        onAddRecentColor = viewModel::addColorToRecent,
        onToggleEditingMode = viewModel::toggleEditingMode,
        onStrokeFinished = viewModel::addStroke,
        onUndo = viewModel::undoStroke,
        onClearStrokes = viewModel::clearStrokes,
        onBack = { viewModel.onBackRequested(onNavigateBack) },
        onDiscardDismiss = viewModel::dismissDiscardDialog,
        onDiscardConfirm = onNavigateBack,
        onSave = viewModel::saveTheme
    )
}

// ==========================================
// ROOT COMPOSABLE
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThemeEditorRoot(
    uiState: CustomThemeEditorUiState,
    onNameChange: (String) -> Unit,
    onImagePicked: (String?) -> Unit,
    onApplyEditedImage: (Float, Float, Float, Float) -> Unit,
    onCancelEditedImage: () -> Unit,
    onSolidBackgroundSelected: (Long) -> Unit,
    onGradientStartSelected: (Long) -> Unit,
    onGradientEndSelected: (Long) -> Unit,
    onPrimaryFocus: () -> Unit,
    onFocusedColorSelected: (Long) -> Unit,
    onIconSelected: (Int) -> Unit,
    onBrushColorSelected: (Long) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onBrushTypeSelected: (BrushType) -> Unit,
    onEraserChanged: (Boolean) -> Unit,
    onSetScreen: (EditorScreenState) -> Unit,
    onSetEditMode: (EditMode) -> Unit,
    onSetGradientNode: (GradientNode) -> Unit,
    onAddRecentColor: (Long) -> Unit,
    onToggleEditingMode: () -> Unit,
    onStrokeFinished: (DrawStroke) -> Unit,
    onUndo: () -> Unit,
    onClearStrokes: () -> Unit,
    onBack: () -> Unit,
    onDiscardDismiss: () -> Unit,
    onDiscardConfirm: () -> Unit,
    onSave: () -> Unit
) {
    val isFinalPreview = uiState.currentScreen == EditorScreenState.FinalPreview
    
    // Preview luôn scale 1f (full width) để không bị viền đen 2 bên
    val animatedPreviewScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "previewScale"
    )

    var editorZoom by remember { mutableFloatStateOf(1f) }
    var editorPan by remember { mutableStateOf(Offset.Zero) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        DisposableEffect(Unit) {
            val originalColor = window.statusBarColor
            val originalLight = insetsController.isAppearanceLightStatusBars

            onDispose {
                window.statusBarColor = originalColor
                insetsController.isAppearanceLightStatusBars = originalLight
            }
        }

        SideEffect {
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            insetsController.isAppearanceLightStatusBars = uiState.editingMode == EditorAppearanceMode.Light
        }
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        if (!isFinalPreview && uiState.currentScreen == EditorScreenState.Home) {
            editorZoom = (editorZoom * zoomChange).coerceIn(1f, 2.8f)
            editorPan = if (editorZoom <= 1.02f) Offset.Zero else Offset(
                x = (editorPan.x + panChange.x).coerceIn(-220f, 220f),
                y = (editorPan.y + panChange.y).coerceIn(-260f, 260f)
            )
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        onImagePicked(uri?.toString())
        if (uri != null) onSetScreen(EditorScreenState.EditComponents)
    }

    BackHandler(onBack = onBack)

    var isPickingColor by remember { mutableStateOf(false) }
    var cachedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var pickOffset by remember { mutableStateOf(Offset.Zero) }
    var pickedColor by remember { mutableStateOf(Color.White) }
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    LaunchedEffect(uiState.currentScreen, uiState.activeEditMode) {
        isPickingColor = false
    }

    val startColorPicking: () -> Unit = {
        coroutineScope.launch {
            try {
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                // Must copy to a software bitmap because getPixel() is not supported on HARDWARE bitmaps
                cachedBitmap = bitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, false)
                isPickingColor = true
            } catch (e: Exception) {
                // Fallback or handle error silently
            }
        }
    }

    // Status bar height — dùng để offset preview bên dưới system bar
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // ── ROOT: Nền đen cố định (không phân biệt light/dark mode) ─────────────
    Box(modifier = Modifier.fillMaxSize().background(Color.Black).drawWithContent {
        graphicsLayer.record {
            this@drawWithContent.drawContent()
        }
        drawLayer(graphicsLayer)
    }) {

        // ── LAYER 1: Preview Box (luôn render, không bao giờ bị re-create) ──
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = statusBarTopPadding + 2.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 110.dp)
                .clip(RoundedCornerShape(30.dp))
                .graphicsLayer {
                    scaleX = animatedPreviewScale * editorZoom
                    scaleY = animatedPreviewScale * editorZoom
                    translationX = editorPan.x
                    translationY = editorPan.y
                }
                .transformable(transformState)
        ) {
            ThemePreviewCapture(
                uiState = uiState,
                showFullPreview = isFinalPreview,
                modifier = Modifier.fillMaxSize(),
                onPrimaryFocus = onPrimaryFocus,
                onIconSelected = onIconSelected,
                onLightModeSelected = { if (uiState.editingMode == EditorAppearanceMode.Dark) onToggleEditingMode() },
                onDarkModeSelected = { if (uiState.editingMode == EditorAppearanceMode.Light) onToggleEditingMode() },
                onToggleAppearance = if (!isFinalPreview) onToggleEditingMode else null,
                onStrokeFinished = onStrokeFinished
            )
        }

        // ── LAYER 2: Screen Overlays ──────────────────────────────────────
        Crossfade(
            targetState = uiState.currentScreen,
            animationSpec = tween(durationMillis = 280),
            label = "screenTransition"
        ) { screen ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (screen) {
                    is EditorScreenState.Home -> HomeOverlay(
                        hasBackgroundSelected = uiState.hasBackgroundSelected(),
                        onClose = onBack,
                        onImageClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onSolidClick = { onSetScreen(EditorScreenState.SolidBg) },
                        onGradientClick = { onSetScreen(EditorScreenState.GradientBg) },
                        onContinueEdit = { onSetScreen(EditorScreenState.EditComponents) }
                    )

                    is EditorScreenState.SolidBg -> SolidBgOverlay(
                        uiState = uiState,
                        onBack = { onSetScreen(EditorScreenState.Home) },
                        onApply = { onSetScreen(EditorScreenState.EditComponents) },
                        onColorSelected = { color ->
                            onSolidBackgroundSelected(color)
                        },
                        onAddRecentColor = onAddRecentColor,
                        onEyedropperClick = startColorPicking,
                        onToggleAppearance = onToggleEditingMode
                    )

                    is EditorScreenState.GradientBg -> GradientBgOverlay(
                        uiState = uiState,
                        onBack = { onSetScreen(EditorScreenState.Home) },
                        onApply = { onSetScreen(EditorScreenState.EditComponents) },
                        onNodeSelected = onSetGradientNode,
                        onColorSelected = { color ->
                            if (uiState.activeGradientNode == GradientNode.Start) {
                                onGradientStartSelected(color)
                            } else {
                                onGradientEndSelected(color)
                            }
                        },
                        onAddRecentColor = onAddRecentColor,
                        onEyedropperClick = startColorPicking,
                        onToggleAppearance = onToggleEditingMode
                    )

                    is EditorScreenState.EditComponents -> EditComponentsOverlay(
                        uiState = uiState,
                        // Back từ EditComponents → về Home (không hỏi discard ở đây)
                        onBack = { onSetScreen(EditorScreenState.Home) },
                        onSetEditMode = onSetEditMode,
                        onNameChange = onNameChange,
                        onPreviewClick = { onSetScreen(EditorScreenState.FinalPreview) },
                        onSaveClick = onSave,
                        onBrushColorSelected = { color ->
                            onBrushColorSelected(color)
                        },
                        onAddRecentColor = onAddRecentColor,
                        onBrushSizeChange = onBrushSizeChange,
                        onBrushTypeSelected = onBrushTypeSelected,
                        onEraserChanged = onEraserChanged,
                        onUndo = onUndo,
                        onClearStrokes = onClearStrokes,
                        onFocusedColorSelected = { color ->
                            onFocusedColorSelected(color)
                        },
                        isDarkMode = uiState.editingMode == EditorAppearanceMode.Dark,
                        onToggleAppearance = onToggleEditingMode,
                        onEyedropperClick = startColorPicking
                    )

                    is EditorScreenState.FinalPreview -> FinalPreviewOverlay(
                        uiState = uiState,
                        onBack = { onSetScreen(EditorScreenState.EditComponents) },
                        onSave = onSave,
                        isDarkMode = uiState.editingMode == EditorAppearanceMode.Dark,
                        onToggleAppearance = onToggleEditingMode
                    )
                }
            }
        }

        // ── LAYER 3: Saving Indicator ─────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isSaving,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .pointerInput(Unit) { detectTapGestures { } }, // Chặn thao tác nhấn khi đang lưu
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    // ── DIALOGS (ngoài Box chính để tránh clipping) ───────────────────────
    if (uiState.pendingBackgroundUri != null) {
        BackgroundTransformDialog(
            imageUri = uiState.pendingBackgroundUri,
            onApply = onApplyEditedImage,
            onDismiss = onCancelEditedImage
        )
    }

    // Cảnh báo discard
    if (uiState.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = onDiscardDismiss,
            title = { Text(stringResource(R.string.custom_theme_discard_title)) },
            text = { Text(stringResource(R.string.custom_theme_discard_desc)) },
            confirmButton = {
                TextButton(onClick = onDiscardConfirm) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDiscardDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    var boxSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    // Lớp phủ Eyedropper (Color Picker từ màn hình)
    if (isPickingColor && cachedBitmap != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { boxSize = it.size }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (cachedBitmap != null && boxSize.width > 0 && boxSize.height > 0) {
                                pickOffset = offset
                                val scaleX = cachedBitmap!!.width.toFloat() / boxSize.width
                                val scaleY = cachedBitmap!!.height.toFloat() / boxSize.height
                                val x = (offset.x * scaleX).toInt().coerceIn(0, cachedBitmap!!.width - 1)
                                val y = (offset.y * scaleY).toInt().coerceIn(0, cachedBitmap!!.height - 1)
                                try {
                                    val pixelColor = cachedBitmap!!.getPixel(x, y)
                                    // Bỏ qua màu đen trong suốt hoàn toàn
                                    if (pixelColor != 0) {
                                        pickedColor = Color(pixelColor)
                                    }
                                } catch (e: Exception) {
                                    // Ignored
                                }
                            }
                        },
                        onDragEnd = {
                            isPickingColor = false
                            val pickedColorLong = pickedColor.toArgb().toLong() and 0xFFFFFFFFL
                            when {
                                uiState.currentScreen == EditorScreenState.SolidBg -> {
                                    onSolidBackgroundSelected(pickedColorLong)
                                    onAddRecentColor(pickedColorLong)
                                }
                                uiState.currentScreen == EditorScreenState.GradientBg -> {
                                    if (uiState.activeGradientNode == GradientNode.Start) {
                                        onGradientStartSelected(pickedColorLong)
                                    } else {
                                        onGradientEndSelected(pickedColorLong)
                                    }
                                    onAddRecentColor(pickedColorLong)
                                }
                                uiState.currentScreen == EditorScreenState.EditComponents -> {
                                    if (uiState.activeEditMode == EditMode.Draw) {
                                        onBrushColorSelected(pickedColorLong)
                                        onAddRecentColor(pickedColorLong)
                                    } else if (uiState.activeEditMode == EditMode.Palette) {
                                        onFocusedColorSelected(pickedColorLong)
                                        onAddRecentColor(pickedColorLong)
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            isPickingColor = false
                        }
                    ) { change, _ ->
                        if (cachedBitmap != null && boxSize.width > 0 && boxSize.height > 0) {
                            pickOffset = change.position
                            val scaleX = cachedBitmap!!.width.toFloat() / boxSize.width
                            val scaleY = cachedBitmap!!.height.toFloat() / boxSize.height
                            val x = (pickOffset.x * scaleX).toInt().coerceIn(0, cachedBitmap!!.width - 1)
                            val y = (pickOffset.y * scaleY).toInt().coerceIn(0, cachedBitmap!!.height - 1)
                            val pixel = cachedBitmap!!.getPixel(x, y)
                            pickedColor = Color(pixel or 0xFF000000.toInt())
                        }
                    }
                }
        ) {
            // UI Hint
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.custom_theme_drag_finger),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            // Loupe cursor
            val cursorSize = 60.dp
            val pxSize = with(androidx.compose.ui.platform.LocalDensity.current) { cursorSize.toPx() }
            
            Box(
                modifier = Modifier
                    .offset { 
                        androidx.compose.ui.unit.IntOffset(
                            x = (pickOffset.x - pxSize / 2).toInt(),
                            y = (pickOffset.y - pxSize - 40f).toInt()
                        )
                    }
                    .size(cursorSize)
                    .background(pickedColor, CircleShape)
                    .border(4.dp, Color.White, CircleShape)
            )
        }
    }
}

// ==========================================
// SCREEN 1: HOME OVERLAY
// ==========================================

@Composable
private fun BoxScope.HomeOverlay(
    hasBackgroundSelected: Boolean,
    onClose: () -> Unit,
    onImageClick: () -> Unit,
    onSolidClick: () -> Unit,
    onGradientClick: () -> Unit,
    onContinueEdit: () -> Unit
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Top-left: X để thoát
    IconButton(
        onClick = onClose,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(top = statusBarTopPadding + 16.dp, start = 8.dp)
            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
    ) {
        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.content_desc_close), tint = Color.White)
    }

    // Top-right: Continue Edit (nếu có background)
    if (hasBackgroundSelected) {
        Button(
            onClick = onContinueEdit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = statusBarTopPadding + 16.dp, end = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit", fontSize = 14.sp)
        }
    }

    // Bottom-left: Expandable menu
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 40.dp, start = 16.dp)
    ) {
        ExpandableBackgroundMenu(
            onImageClick = onImageClick,
            onSolidClick = onSolidClick,
            onGradientClick = onGradientClick
        )
    }
}

@Composable
fun ExpandableBackgroundMenu(
    onImageClick: () -> Unit,
    onSolidClick: () -> Unit,
    onGradientClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nút Image chính – toggle expand/collapse
        IconButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier
                .size(52.dp)
                .background(Color.White.copy(alpha = 0.18f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.AutoMirrored.Rounded.KeyboardArrowLeft else Icons.Rounded.Image,
                contentDescription = stringResource(R.string.content_desc_background),
                tint = Color.White
            )
        }

        // Expanded: 3 icon — Image gallery, Solid color, Gradient
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    + fadeIn(animationSpec = tween(200)),
            exit = shrinkHorizontally(animationSpec = tween(180)) + fadeOut(animationSpec = tween(150))
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Gallery / Image
                ExpandMenuIconButton(
                    icon = Icons.Rounded.PhotoLibrary,
                    label = "Gallery",
                    onClick = {
                        isExpanded = false
                        onImageClick()
                    }
                )
                // Solid color
                ExpandMenuIconButton(
                    icon = Icons.Rounded.FormatColorFill,
                    label = "Solid",
                    onClick = {
                        isExpanded = false
                        onSolidClick()
                    }
                )
                // Gradient color
                ExpandMenuIconButton(
                    icon = Icons.Rounded.Gradient,
                    label = "Gradient",
                    onClick = {
                        isExpanded = false
                        onGradientClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun ExpandMenuIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(Color.White.copy(alpha = 0.18f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
    ) {
        Icon(icon, contentDescription = label, tint = Color.White)
    }
}

// ==========================================
// SCREEN 2: SOLID BACKGROUND OVERLAY
// ==========================================

@Composable
fun BoxScope.SolidBgOverlay(
    uiState: CustomThemeEditorUiState,
    onBack: () -> Unit,
    onApply: () -> Unit,
    onColorSelected: (Long) -> Unit,
    onAddRecentColor: (Long) -> Unit,
    onEyedropperClick: () -> Unit,
    onToggleAppearance: () -> Unit
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Top bar: ← và ✓
    TopEditorBar(
        onBack = onBack,
        onApply = onApply,
        isDarkMode = uiState.editingMode == EditorAppearanceMode.Dark,
        onToggleAppearance = onToggleAppearance
    )

    // Bottom color picker (scrollable, không có eyedropper)
    ColorScrollablePickerBar(
        recentColors = uiState.recentColors,
        currentColor = uiState.solidBackgroundColor,
        onColorSelected = onColorSelected,
        onAddRecentColor = onAddRecentColor,
        onEyedropperClick = onEyedropperClick,
        modifier = Modifier.align(Alignment.BottomCenter),
        showEyedropper = false
    )
}

// ==========================================
// SCREEN 3: GRADIENT BACKGROUND OVERLAY
// ==========================================

@Composable
fun BoxScope.GradientBgOverlay(
    uiState: CustomThemeEditorUiState,
    onBack: () -> Unit,
    onApply: () -> Unit,
    onNodeSelected: (GradientNode) -> Unit,
    onColorSelected: (Long) -> Unit,
    onAddRecentColor: (Long) -> Unit,
    onEyedropperClick: () -> Unit,
    onToggleAppearance: () -> Unit
) {
    TopEditorBar(
        onBack = onBack,
        onApply = onApply,
        isDarkMode = uiState.editingMode == EditorAppearanceMode.Dark,
        onToggleAppearance = onToggleAppearance
    )

    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gradient node selector (Start / End pill)
        GradientNodeSelector(
            activeNode = uiState.activeGradientNode,
            startColor = uiState.gradientStartColor,
            endColor = uiState.gradientEndColor,
            onNodeSelected = onNodeSelected,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Scrollable color bar (không có eyedropper)
        ColorScrollablePickerBar(
            recentColors = uiState.recentColors,
            currentColor = if (uiState.activeGradientNode == GradientNode.Start) uiState.gradientStartColor else uiState.gradientEndColor,
            onColorSelected = onColorSelected,
            onAddRecentColor = onAddRecentColor,
            onEyedropperClick = onEyedropperClick,
            showEyedropper = false
        )
    }
}

// ==========================================
// SCREEN 4: EDIT COMPONENTS OVERLAY
// ==========================================

@Composable
fun BoxScope.EditComponentsOverlay(
    uiState: CustomThemeEditorUiState,
    onBack: () -> Unit,
    onSetEditMode: (EditMode) -> Unit,
    onNameChange: (String) -> Unit,
    onPreviewClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBrushColorSelected: (Long) -> Unit,
    onAddRecentColor: (Long) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onBrushTypeSelected: (BrushType) -> Unit,
    onEraserChanged: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onClearStrokes: () -> Unit,
    onFocusedColorSelected: (Long) -> Unit,
    isDarkMode: Boolean,
    onToggleAppearance: () -> Unit,
    onEyedropperClick: () -> Unit
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    when (uiState.activeEditMode) {
        EditMode.None -> {
            // ── Back button (top-left, trong preview) ────────────────────
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = statusBarTopPadding + 20.dp, start = 16.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.content_desc_back),
                    tint = Color.White
                )
            }

            // ── Toolbar icons (top-right): Draw → Palette → Light/Dark ─────────────
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = statusBarTopPadding + 20.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EditorIconButton(
                    icon = Icons.Rounded.Brush,
                    label = "Draw",
                    onClick = { onSetEditMode(EditMode.Draw) }
                )
                EditorIconButton(
                    icon = Icons.Rounded.Palette,
                    label = "Colors",
                    onClick = { onSetEditMode(EditMode.Palette) }
                )
                EditorIconButton(
                    icon = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    label = "Theme Mode",
                    onClick = onToggleAppearance
                )
            }

            // ── Main bottom bar: Preview | Name | Save ───────────────────
            AnimatedVisibility(
                visible = true,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                EditComponentsBottomBar(
                    name = uiState.name,
                    onNameChange = onNameChange,
                    onPreviewClick = onPreviewClick,
                    onSaveClick = onSaveClick
                )
            }
        }

        EditMode.Draw -> {
            // ── Draw toolbar (top-center, ngoài preview) ──────────────────
            DrawToolbar(
                uiState = uiState,
                onUndo = onUndo,
                onClearStrokes = onClearStrokes,
                // ✓ Check icon để done vẽ
                onDone = { onSetEditMode(EditMode.None) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = statusBarTopPadding + 16.dp)
            )

            // ── 5 Draw Tools (bottom-center, in preview area) ──────────────────
            DrawToolsRow(
                uiState = uiState,
                onBrushTypeSelected = onBrushTypeSelected,
                onEraserChanged = onEraserChanged,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp) // 110dp for Color picker + 10dp gap
            )

            // ── Vertical size slider (left edge) ──────────────────────────
            VerticalSizeSlider(
                value = uiState.brushSize,
                onValueChange = onBrushSizeChange,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
            )

            // ── Color picker bar (bottom) ──────────────────────────────────
            ColorScrollablePickerBar(
                recentColors = uiState.recentColors,
                currentColor = uiState.brushColor,
                onColorSelected = onBrushColorSelected,
                onAddRecentColor = onAddRecentColor,
                onEyedropperClick = onEyedropperClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        EditMode.Palette -> {
            // ── Toolbar icons (top-right): Theme Mode toggle & Done ──────
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = statusBarTopPadding + 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditorIconButton(
                    icon = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    label = "Theme Mode",
                    onClick = onToggleAppearance
                )

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { onSetEditMode(EditMode.None) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.content_desc_done), tint = Color.Black, modifier = Modifier.size(22.dp))
                }
            }

            // ── Color picker bar (bottom) ──────────────────────────────────
            ColorScrollablePickerBar(
                recentColors = uiState.recentColors,
                currentColor = when (uiState.colorFocusTarget) {
                    ColorFocusTarget.Primary -> uiState.primaryColor
                    ColorFocusTarget.Icon -> uiState.iconColor
                },
                onColorSelected = onFocusedColorSelected,
                onAddRecentColor = onAddRecentColor,
                onEyedropperClick = onEyedropperClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun EditorIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
    ) {
        Icon(icon, contentDescription = label, tint = Color.White)
    }
}

@Composable
private fun EditComponentsBottomBar(
    name: String,
    onNameChange: (String) -> Unit,
    onPreviewClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.72f))
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Preview button (Redesigned to a sleek icon)
        IconButton(
            onClick = onPreviewClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                Icons.Rounded.Visibility,
                contentDescription = stringResource(R.string.content_desc_preview),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Tên theme (chỉ đọc) + icon Edit
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = name.ifBlank { "My Custom Theme" },
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = { showRenameDialog = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.content_desc_edit_name),
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Save button
        Button(
            onClick = onSaveClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(stringResource(R.string.save), style = MaterialTheme.typography.labelMedium)
        }
    }

    // Rename dialog
    if (showRenameDialog) {
        var tempName by remember { mutableStateOf(name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.custom_theme_rename)) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.custom_theme_name)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onNameChange(tempName)
                    showRenameDialog = false
                }) { Text(stringResource(R.string.apply), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun BasicNameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = Color.White,
            textAlign = TextAlign.Center
        ),
        placeholder = {
            Text(
                "Theme name",
                color = Color.White.copy(alpha = 0.45f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White.copy(alpha = 0.4f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            cursorColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp)
    )
}

// ==========================================
// SCREEN 5: FINAL PREVIEW OVERLAY
// ==========================================

@Composable
private fun BoxScope.FinalPreviewOverlay(
    uiState: CustomThemeEditorUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    isDarkMode: Boolean,
    onToggleAppearance: () -> Unit
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Top-left: < quay lại EditComponents
    IconButton(
        onClick = onBack,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(top = statusBarTopPadding + 16.dp, start = 8.dp)
            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.content_desc_back),
            tint = Color.White
        )
    }

    // Top-right: icon Light/Dark toggle (1 nút duy nhất như màn hình edit)
    EditorIconButton(
        icon = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
        label = "Toggle Mode",
        onClick = onToggleAppearance,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = statusBarTopPadding + 16.dp, end = 16.dp)
    )

    // Bottom bar: tên (chỉ đọc) + nút Save
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.65f))
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = uiState.name.ifBlank { "My Theme" },
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(16.dp))
        Button(
            onClick = onSave,
            enabled = !uiState.isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// SHARED: TOP EDITOR BAR (Screen 2 & 3)
// ==========================================

@Composable
fun BoxScope.TopEditorBar(
    onBack: () -> Unit,
    onApply: () -> Unit,
    isDarkMode: Boolean? = null,
    onToggleAppearance: (() -> Unit)? = null
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .padding(top = statusBarTopPadding + 16.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ← Quay lại
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.35f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = Color.White
            )
        }

        // Toggle light/dark mode icon
        if (isDarkMode != null && onToggleAppearance != null) {
            IconButton(
                onClick = onToggleAppearance,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.35f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = "Toggle Appearance Mode",
                    tint = Color.White
                )
            }
        }

        // ✓ Áp dụng và sang màn tiếp
        IconButton(
            onClick = onApply,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.35f), CircleShape)
        ) {
            Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.content_desc_apply), tint = Color.White)
        }
    }
}

// ==========================================
// GRADIENT NODE SELECTOR
// ==========================================

@Composable
fun GradientNodeSelector(
    activeNode: GradientNode,
    startColor: Long,
    endColor: Long,
    onNodeSelected: (GradientNode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(28.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GradientNodeTab(
            label = "Start",
            color = startColor,
            isActive = activeNode == GradientNode.Start,
            onClick = { onNodeSelected(GradientNode.Start) }
        )
        GradientNodeTab(
            label = "End",
            color = endColor,
            isActive = activeNode == GradientNode.End,
            onClick = { onNodeSelected(GradientNode.End) }
        )
    }
}

@Composable
private fun GradientNodeTab(
    label: String,
    color: Long,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bgAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.22f else 0f,
        animationSpec = tween(200),
        label = "nodeTabBg"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.White.copy(alpha = bgAlpha), RoundedCornerShape(24.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Color swatch
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color(color), CircleShape)
                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ==========================================
// COLOR PAGED PICKER BAR
// ==========================================

/**
 * 4 trang màu preset cố định. Khi user thêm màu custom → tạo thêm trang mới.
 */
private val PresetColorPages: List<List<Long>> = listOf(
    // Trang 1: Màu cơ bản (đen-trắng-primary)
    listOf(
        0xFFFFFFFF, 0xFF1A1A1A, 0xFF2196F3, 0xFF4CAF50,
        0xFFFFEB3B, 0xFFFF9800, 0xFFD32F2F, 0xFFE91E63, 0xFF9C27B0
    ),
    // Trang 2: Xám đậm → xám nhạt
    listOf(
        0xFF424242, 0xFF616161, 0xFF757575, 0xFF9E9E9E,
        0xFFBDBDBD, 0xFFDDDDDD, 0xFFEEEEEE, 0xFFF5F5F5, 0xFFFFFFFF
    ),
    // Trang 3: Màu trung tính ấm
    listOf(
        0xFF795548, 0xFF8D6E63, 0xFFA1887F, 0xFFBCAAA4,
        0xFFD7CCC8, 0xFFEFEBE9, 0xFFFFF7EC, 0xFFFFF3E0, 0xFFFBE9E7
    ),
    // Trang 4: Màu ấm (đỏ-hồng-cam-nâu)
    listOf(
        0xFFD32F2F, 0xFFE57373, 0xFFFFCDD2, 0xFFFFCCBC,
        0xFFFFAB91, 0xFFBCAAA4, 0xFF8D6E63, 0xFF4E342E, 0xFF1B5E20
    )
)

/** Chunk recentColors thành các trang 9 màu để tạo custom pages */
private fun List<Long>.chunkedToPages(): List<List<Long>> =
    if (isEmpty()) emptyList() else chunked(9)

@Composable
fun ColorScrollablePickerBar(
    recentColors: List<Long>,
    currentColor: Long?,
    onColorSelected: (Long) -> Unit,
    onAddRecentColor: (Long) -> Unit,
    onEyedropperClick: () -> Unit,
    modifier: Modifier = Modifier,
    showEyedropper: Boolean = true
) {
    var showColorSheet by remember { mutableStateOf(false) }
    var pendingCustomColor by remember { mutableLongStateOf(0xFFFF0000L) }

    // Màu mới nhất ở đầu (bên trái)
    val allColors = remember(recentColors) {
        recentColors + PresetColorPages.flatten()
    }

    // Layout: Row tổng = Box chứa 2 icon cố định + LazyRow cuộn phía sau
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 10.dp)
            .height(62.dp)
    ) {
        // LazyRow các màu — điều chỉnh padding trái để tránh 2 icon hoặc 1 icon cố định
        LazyRow(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(start = if (showEyedropper) 118.dp else 66.dp, end = 16.dp)
        ) {
            items(allColors) { color ->
                ColorSwatchCircle(
                    color = color,
                    onClick = { onColorSelected(color) }
                )
            }
        }

        // 2 icon cố định bên trái (nằm trên LazyRow)
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Eyedropper cố định (chỉ hiện khi showEyedropper = true)
            if (showEyedropper) {
                val containerColor = if (currentColor != null) Color(currentColor) else Color.White.copy(alpha = 0.15f)
                val tintColor = if (currentColor != null) {
                    if (Color(currentColor).perceivedBrightness() < 0.5f) Color.White else Color.Black
                } else Color.White

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(containerColor, CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onEyedropperClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Colorize,
                        contentDescription = stringResource(R.string.content_desc_pick_color),
                        tint = tintColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Icon + (Tạo màu) cố định
            AddColorCircleButton(onClick = { showColorSheet = true })
        }
    }

    // Color picker bottom sheet
    if (showColorSheet) {
        ColorPickerBottomSheet(
            selectedColor = pendingCustomColor,
            onColorSelected = { color ->
                pendingCustomColor = color
                onColorSelected(color)
            },
            onApply = { color ->
                onAddRecentColor(color)
            },
            allowGradient = false,
            mode = BackgroundFillMode.Solid,
            onModeChange = {},
            gradientStartColor = 0L,
            gradientEndColor = 0L,
            activeGradientStop = 0,
            onActiveGradientStopChange = {},
            onGradientStartColorSelected = {},
            onGradientEndColorSelected = {},
            onDismiss = { showColorSheet = false }
        )
    }
}

@Composable
private fun AddColorCircleButton(onClick: () -> Unit) {
    val rainbowBrush = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFFFF4D4D), Color(0xFFFFB84D), Color(0xFFFFF176),
                Color(0xFF66BB6A), Color(0xFF4FC3F7), Color(0xFF7E57C2),
                Color(0xFFFF4D4D)
            )
        )
    }
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(rainbowBrush, CircleShape)
            .padding(2.dp)
            .background(Color(0xFF1A1A1A), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.Add,
            contentDescription = stringResource(R.string.content_desc_add_color),
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ColorSwatchCircle(
    color: Long,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(color))
            .border(1.5.dp, if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.2f), CircleShape)
            .clickable(onClick = onClick)
    )
}

// ==========================================
// DRAW TOOLBAR
// ==========================================

@Composable
fun DrawToolbar(
    uiState: CustomThemeEditorUiState,
    onUndo: () -> Unit,
    onClearStrokes: () -> Unit,
    onDone: () -> Unit, // ✓ Check icon để done
    modifier: Modifier = Modifier
) {
    // Hàng 1: Undo (Back) và Done (Tick)
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nhóm bên trái: Undo và Clear
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo (Back)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (uiState.strokes.isNotEmpty()) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.25f),
                        CircleShape
                    )
                    .border(2.dp, Color.White.copy(alpha = if (uiState.strokes.isNotEmpty()) 0.8f else 0.25f), CircleShape)
                    .clickable(enabled = uiState.strokes.isNotEmpty(), onClick = onUndo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = stringResource(R.string.content_desc_undo),
                    tint = if (uiState.strokes.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Clear All (Xóa toàn bộ)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (uiState.strokes.isNotEmpty()) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.25f),
                        CircleShape
                    )
                    .border(2.dp, Color.White.copy(alpha = if (uiState.strokes.isNotEmpty()) 0.8f else 0.25f), CircleShape)
                    .clickable(enabled = uiState.strokes.isNotEmpty(), onClick = onClearStrokes),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.content_desc_clear_all),
                    tint = if (uiState.strokes.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Done (Tick)
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color.White, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .clickable(onClick = onDone),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = stringResource(R.string.content_desc_done),
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DrawToolsRow(
    uiState: CustomThemeEditorUiState,
    onBrushTypeSelected: (BrushType) -> Unit,
    onEraserChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Cọ nét mảnh (Pen)
        DrawToolIconButton(
            selected = uiState.brushType == BrushType.Fine && !uiState.isEraser,
            onClick = { onBrushTypeSelected(BrushType.Fine) },
            icon = Icons.Rounded.Edit,
            label = "Pen"
        )
        // 2. Cọ dạ quang (Highlighter)
        DrawToolIconButton(
            selected = uiState.brushType == BrushType.Bold && !uiState.isEraser,
            onClick = { onBrushTypeSelected(BrushType.Bold) },
            icon = Icons.Rounded.KeyboardArrowUp,
            label = "Highlighter"
        )
        // 3. Cọ viền Neon (Glow)
        DrawToolIconButton(
            selected = uiState.brushType == BrushType.Pencil && !uiState.isEraser,
            onClick = { onBrushTypeSelected(BrushType.Pencil) },
            icon = Icons.Rounded.AutoAwesome,
            label = "Neon"
        )
        // 4. Tẩy (Eraser)
        DrawToolIconButton(
            selected = uiState.isEraser,
            onClick = { onEraserChanged(true) },
            icon = Icons.Rounded.CleaningServices,
            label = "Eraser"
        )
        // 5. Cọ họa tiết (Heart/Sparkle)
        DrawToolIconButton(
            selected = uiState.brushType == BrushType.Spray && !uiState.isEraser,
            onClick = { onBrushTypeSelected(BrushType.Spray) },
            icon = Icons.Rounded.Favorite,
            label = "Sparkle"
        )
    }
}

// ==========================================
// VERTICAL SIZE SLIDER
// ==========================================

@Composable
fun VerticalSizeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val range = 2f..40f

    Canvas(
        modifier = modifier
            .height(240.dp)
            .width(40.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val y = change.position.y
                    val fraction = 1f - (y / size.height.toFloat()).coerceIn(0f, 1f)
                    val newValue = range.start + fraction * (range.endInclusive - range.start)
                    onValueChange(newValue)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val y = offset.y
                    val fraction = 1f - (y / size.height.toFloat()).coerceIn(0f, 1f)
                    val newValue = range.start + fraction * (range.endInclusive - range.start)
                    onValueChange(newValue)
                }
            }
    ) {
        val fraction = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
        val trackWidth = 2.dp.toPx()
        val thumbRadius = 6.dp.toPx()

        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(size.width / 2, thumbRadius),
            end = Offset(size.width / 2, size.height - thumbRadius),
            strokeWidth = trackWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        val thumbY = size.height - thumbRadius - fraction * (size.height - 2 * thumbRadius)

        drawLine(
            color = Color.White,
            start = Offset(size.width / 2, size.height - thumbRadius),
            end = Offset(size.width / 2, thumbY),
            strokeWidth = trackWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        drawCircle(
            color = Color.White,
            radius = thumbRadius,
            center = Offset(size.width / 2, thumbY)
        )
    }
}

// ==========================================
// THEME PREVIEW CAPTURE (Canvas + Content)
// ==========================================

@Composable
private fun ThemePreviewCapture(
    uiState: CustomThemeEditorUiState,
    showFullPreview: Boolean,
    modifier: Modifier,
    onPrimaryFocus: () -> Unit = {},
    onIconSelected: (Int) -> Unit = {},
    onLightModeSelected: () -> Unit = {},
    onDarkModeSelected: () -> Unit = {},
    onToggleAppearance: (() -> Unit)? = null,
    onStrokeFinished: (DrawStroke) -> Unit
) {
    var activePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val primary = uiState.primaryColor.toComposeColor()
    val solidBackground = uiState.solidBackgroundColor.toComposeColor()
    val isDarkMode = uiState.editingMode == EditorAppearanceMode.Dark
    val isPreviewBackgroundDark = uiState.isPreviewBackgroundDark(fallbackDark = isDarkMode)
    val safeBackgroundScale = uiState.backgroundScale * rotationCoverMultiplier(uiState.backgroundRotation)
    val hasImageBackground = !uiState.backgroundUri.isNullOrBlank()
    val hasVisualBackground = hasImageBackground || uiState.backgroundFillMode == BackgroundFillMode.Gradient
    val previewProtection = remember(isPreviewBackgroundDark, hasVisualBackground) {
        previewProtectionColors(
            darkBackground = isPreviewBackgroundDark,
            hasVisualBackground = hasVisualBackground
        )
    }
    val contentColor = previewProtection.contentColor
    val panelColor = previewProtection.panelColor
    val gradientBrush = remember(uiState.gradientStartColor, uiState.gradientEndColor) {
        Brush.verticalGradient(
            colors = listOf(
                uiState.gradientStartColor.toComposeColor(),
                uiState.gradientEndColor.toComposeColor()
            )
        )
    }
    val backgroundBrush = when {
        hasImageBackground -> null
        uiState.backgroundFillMode == BackgroundFillMode.Gradient -> gradientBrush
        else -> null
    }
    val bottomBarColor = previewProtection.bottomBarColor
    val bottomBarCutoutColor = previewProtection.cameraCutoutColor

    Box(
        modifier = modifier
            .then(
                if (backgroundBrush != null) Modifier.background(backgroundBrush)
                else Modifier.background(solidBackground)
            )
            .then(
                if (uiState.activeEditMode == EditMode.Draw) {
                    Modifier.pointerInput(uiState.brushColor, uiState.brushSize, uiState.brushType, uiState.isEraser) {
                        awaitEachGesture {
                            val strokePoints = mutableListOf<Offset>()
                            var cancelledForTransform = false

                            val firstDown = awaitFirstDown(requireUnconsumed = false)
                            var activePointerId = firstDown.id
                            strokePoints += firstDown.position
                            activePoints = strokePoints.toList()

                            while (true) {
                                val event = awaitPointerEvent()
                                val pressedChanges = event.changes.filter { it.pressed }

                                if (pressedChanges.size > 1) {
                                    cancelledForTransform = true
                                    activePoints = emptyList()
                                    break
                                }

                                val change = pressedChanges.firstOrNull { it.id == activePointerId }
                                    ?: pressedChanges.firstOrNull()
                                    ?: break

                                activePointerId = change.id

                                if (change.position != change.previousPosition) {
                                    strokePoints += change.position
                                    activePoints = strokePoints.toList()
                                    change.consume()
                                }
                            }

                            if (!cancelledForTransform && strokePoints.size > 1) {
                                onStrokeFinished(
                                    DrawStroke(
                                        points = strokePoints,
                                        color = uiState.brushColor,
                                        strokeWidth = uiState.brushSize,
                                        brushType = uiState.brushType,
                                        isEraser = uiState.isEraser
                                    )
                                )
                            }
                            activePoints = emptyList()
                        }
                    }
                } else Modifier
            )
    ) {
        // Image background
        if (!uiState.backgroundUri.isNullOrBlank()) {
            AsyncImage(
                model = uiState.backgroundUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = safeBackgroundScale
                        scaleY = safeBackgroundScale
                        rotationZ = uiState.backgroundRotation
                        translationX = uiState.backgroundOffsetX
                        translationY = uiState.backgroundOffsetY
                    },
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(previewImageScrim(isDarkMode))
            )
        }

        // Canvas strokes
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            uiState.strokes.forEach { drawStroke(it, solidBackground) }
            if (activePoints.size > 1) {
                drawStroke(
                    DrawStroke(
                        points = activePoints,
                        color = uiState.brushColor,
                        strokeWidth = uiState.brushSize,
                        brushType = uiState.brushType,
                        isEraser = uiState.isEraser
                    ),
                    solidBackground
                )
            }
        }

        // Full calendar preview (Screen 5)
        if (showFullPreview) {
            ThemeCalendarMockScreen(
                isDarkMode = isDarkMode,
                primary = primary,
                iconColors = uiState.iconColors,
                contentColor = contentColor,
                panelColor = panelColor,
                bottomBarColor = bottomBarColor,
                bottomBarCutoutColor = bottomBarCutoutColor,
                onLightModeSelected = onLightModeSelected,
                onDarkModeSelected = onDarkModeSelected,
                modifier = Modifier.fillMaxSize()
            )
        } else {

            // Preview mock UI elements: show on EditComponents mode (Palette or None, hide on Draw so it doesn't obstruct drawing)
            if (uiState.currentScreen == EditorScreenState.EditComponents && uiState.activeEditMode != EditMode.Draw) {
                PreviewMoodIconRow(
                    colors = uiState.iconColors,
                    selectedIndex = if (uiState.activeEditMode == EditMode.Palette && uiState.colorFocusTarget == ColorFocusTarget.Icon) uiState.selectedIconIndex else -1,
                    onSelected = onIconSelected,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 80.dp)
                )
                MockAppBottomNavBar(
                    primary = primary,
                    backgroundColor = bottomBarColor,
                    centerCutoutColor = bottomBarCutoutColor,
                    emphasizeIcons = uiState.colorFocusTarget == ColorFocusTarget.Primary,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onPrimarySelected = onPrimaryFocus
                )
            }
        }
    }
}

// ==========================================
// APPEARANCE MODE FAB
// ==========================================

@Composable
private fun AppearanceModeFab(
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        shadowElevation = 12.dp,
        color = if (isDarkMode) Color.Black.copy(alpha = 0.52f) else Color.White.copy(alpha = 0.52f)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                contentDescription = stringResource(if (isDarkMode) R.string.dark else R.string.light),
                tint = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
            )
        }
    }
}

// ==========================================
// THEME CALENDAR MOCK SCREEN (Preview)
// ==========================================

@Composable
private fun ThemeCalendarMockScreen(
    isDarkMode: Boolean,
    primary: Color,
    iconColors: List<Long>,
    contentColor: Color,
    panelColor: Color,
    bottomBarColor: Color,
    bottomBarCutoutColor: Color,
    onLightModeSelected: () -> Unit,
    onDarkModeSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Đặt lịch nhỏ hơn và căn giữa theo trục dọc
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .fillMaxHeight(0.65f) // Thu nhỏ chiều cao xuống 65%
                .padding(start = 20.dp, end = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: chỉ hiển thị tên tháng và căn giữa
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.custom_theme_mock_month),
                        color = contentColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    color = panelColor,
                    tonalElevation = if (isDarkMode) 6.dp else 0.dp,
                    shadowElevation = if (isDarkMode) 14.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MockCalendarWeekHeader(contentColor = contentColor)
                        MockCalendarGrid(
                            primary = primary,
                            iconColors = iconColors,
                            contentColor = contentColor,
                            panelColor = panelColor,
                            transparentBackground = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        MockAppBottomNavBar(
            primary = primary,
            backgroundColor = bottomBarColor,
            centerCutoutColor = bottomBarCutoutColor,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun MiniModeIcon(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    panelColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (selected) tint.copy(alpha = 0.18f) else panelColor
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(34.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun MockCalendarWeekHeader(contentColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = contentColor.copy(alpha = 0.62f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MockCalendarGrid(
    primary: Color,
    iconColors: List<Long>,
    contentColor: Color,
    panelColor: Color,
    transparentBackground: Boolean = false,
    modifier: Modifier = Modifier
) {
    val moodDays = mapOf(2 to 0, 5 to 1, 9 to 2, 14 to 3, 18 to 4, 22 to 0, 27 to 1)
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(5) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(7) { column ->
                        val day = row * 7 + column - 2
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..31) {
                                val moodIndex = moodDays[day]
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (moodIndex != null) iconColors[moodIndex].toComposeColor()
                                                else primary.copy(alpha = 0.16f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (moodIndex != null) {
                                            MoodFaceIcon(
                                                index = moodIndex,
                                                color = iconColors[moodIndex].toComposeColor(),
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = day.toString(),
                                        color = if (moodIndex != null) contentColor else contentColor.copy(alpha = 0.55f),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (moodIndex != null) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (transparentBackground) {
        Box(modifier = modifier.fillMaxWidth()) { content() }
    } else {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = panelColor,
            modifier = modifier.fillMaxWidth()
        ) { content() }
    }
}

// ==========================================
// CANVAS DRAW STROKE
// ==========================================

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStroke(
    stroke: DrawStroke,
    background: Color
) {
    val path = Path()
    stroke.points.forEachIndexed { index, point ->
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    val width = when (stroke.brushType) {
        BrushType.Fine -> stroke.strokeWidth
        BrushType.Bold -> stroke.strokeWidth * 1.8f
        BrushType.Pencil -> stroke.strokeWidth * 0.8f
        BrushType.Spray -> stroke.strokeWidth * 2.2f
    }
    drawPath(
        path = path,
        color = if (stroke.isEraser) Color.Transparent
        else stroke.color.toComposeColor().copy(alpha = if (stroke.brushType == BrushType.Pencil) 0.68f else 1f),
        style = Stroke(width = width, cap = androidx.compose.ui.graphics.StrokeCap.Round),
        blendMode = if (stroke.isEraser) BlendMode.Clear else BlendMode.SrcOver
    )
}

// ==========================================
// MOCK APP BOTTOM NAV BAR
// ==========================================

@Composable
private fun MockAppBottomNavBar(
    primary: Color,
    backgroundColor: Color,
    centerCutoutColor: Color,
    emphasizeIcons: Boolean = false,
    modifier: Modifier = Modifier,
    onPrimarySelected: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp) // Cố định chiều cao thanh nav bar
            .shadow(elevation = 16.dp, spotColor = Color.Black.copy(alpha = 0.10f))
            .clickable(enabled = onPrimarySelected != null) { onPrimarySelected?.invoke() },
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MockNavIcon(Icons.Rounded.CalendarMonth, tint = primary, emphasized = emphasizeIcons)
            MockNavIcon(Icons.Rounded.BarChart, tint = primary, emphasized = emphasizeIcons)
            // Icon Camera thay cho dấu +
            val camSize by animateDpAsState(targetValue = if (emphasizeIcons) 26.dp else 24.dp, label = "camSize")
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(centerCutoutColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(camSize)
                )
            }
            MockNavIcon(Icons.Rounded.Storefront, tint = primary, emphasized = emphasizeIcons)
            MockNavIcon(Icons.Rounded.Person, tint = primary, emphasized = emphasizeIcons)
        }
    }
}

@Composable
private fun MockNavIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    emphasized: Boolean = false
) {
    val size by animateDpAsState(
        targetValue = if (emphasized) 32.dp else 26.dp, 
        label = "iconSize"
    )
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .padding(4.dp)
            .size(size)
    )
}

// ==========================================
// DRAW TOOL ICON BUTTON
// ==========================================

@Composable
private fun DrawToolIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val offsetY by animateDpAsState(
        targetValue = if (selected) (-8).dp else 0.dp,
        animationSpec = tween(200),
        label = "drawToolOffset"
    )
    val bgColor = if (selected) Color.White else Color.Black.copy(alpha = 0.4f)
    val iconColor = if (selected) Color.Black else Color.White

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .size(42.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(2.dp, Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ==========================================
// PREVIEW MOOD ICON ROW
// ==========================================

@Composable
private fun PreviewMoodIconRow(
    colors: List<Long>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.take(5).forEachIndexed { index, color ->
            val selected = selectedIndex == index
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.18f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "moodScale"
            )
            MoodFaceIcon(
                index = index,
                color = color.toComposeColor(),
                modifier = Modifier
                    .size(50.dp)
                    .scale(scale)
                    .shadow(if (selected) 12.dp else 0.dp, CircleShape)
                    .clickable { onSelected(index) }
            )
        }
    }
}

@Composable
private fun MoodFaceIcon(index: Int, color: Color, modifier: Modifier = Modifier) {
    val drawable = when (index) {
        0 -> R.drawable.very_happy
        1 -> R.drawable.happy
        2 -> R.drawable.neutral
        3 -> R.drawable.sad
        else -> R.drawable.very_sad
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(drawable),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.fillMaxSize(0.58f)
        )
    }
}

// ==========================================
// COLOR PICKER BOTTOM SHEET (HSV)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorPickerBottomSheet(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    onApply: (Long) -> Unit,
    onDismiss: () -> Unit,
    allowGradient: Boolean = false,
    mode: BackgroundFillMode = BackgroundFillMode.Solid,
    onModeChange: (BackgroundFillMode) -> Unit = {},
    gradientStartColor: Long = selectedColor,
    gradientEndColor: Long = selectedColor,
    activeGradientStop: Int = 0,
    onActiveGradientStopChange: (Int) -> Unit = {},
    onGradientStartColorSelected: (Long) -> Unit = {},
    onGradientEndColorSelected: (Long) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editingColor = if (allowGradient && mode == BackgroundFillMode.Gradient) {
        if (activeGradientStop == 0) gradientStartColor else gradientEndColor
    } else {
        selectedColor
    }
    var localHue by remember { mutableFloatStateOf(0f) }
    var localSaturation by remember { mutableFloatStateOf(0f) }
    var localValue by remember { mutableFloatStateOf(0f) }
    var hexValue by remember { mutableStateOf(colorToHex(editingColor)) }

    LaunchedEffect(editingColor, mode, activeGradientStop) {
        val hsv = editingColor.toHsv()
        localHue = hsv[0]
        localSaturation = hsv[1]
        localValue = hsv[2]
        hexValue = colorToHex(editingColor)
    }

    fun dispatchColor(color: Long) {
        val hsv = color.toHsv()
        localHue = hsv[0]
        localSaturation = hsv[1]
        localValue = hsv[2]
        hexValue = colorToHex(color)
        if (allowGradient && mode == BackgroundFillMode.Gradient) {
            if (activeGradientStop == 0) onGradientStartColorSelected(color)
            else onGradientEndColorSelected(color)
        } else {
            onColorSelected(color)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (allowGradient) {
                TabRow(selectedTabIndex = if (mode == BackgroundFillMode.Solid) 0 else 1) {
                    Tab(
                        selected = mode == BackgroundFillMode.Solid,
                        onClick = { onModeChange(BackgroundFillMode.Solid) },
                        text = { Text(stringResource(R.string.custom_theme_solid_color)) }
                    )
                    Tab(
                        selected = mode == BackgroundFillMode.Gradient,
                        onClick = { onModeChange(BackgroundFillMode.Gradient) },
                        text = { Text(stringResource(R.string.custom_theme_gradient_color)) }
                    )
                }
            }

            if (allowGradient && mode == BackgroundFillMode.Gradient) {
                GradientStopSelector(
                    startColor = gradientStartColor,
                    endColor = gradientEndColor,
                    selectedStop = activeGradientStop,
                    onSelectedStopChange = onActiveGradientStopChange
                )
            }

            SaturationValuePicker(
                hue = localHue,
                saturation = localSaturation,
                value = localValue,
                onChange = { saturation, value ->
                    localSaturation = saturation
                    localValue = value
                    dispatchColor(colorFromHsv(localHue, saturation, value))
                }
            )

            HueSlider(
                selectedHue = localHue,
                onHueChange = { hue ->
                    localHue = hue
                    dispatchColor(
                        colorFromHsv(
                            hue,
                            localSaturation.coerceAtLeast(0.01f),
                            localValue.coerceAtLeast(0.01f)
                        )
                    )
                }
            )

            // Hex input row
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (allowGradient && mode == BackgroundFillMode.Gradient) {
                                    Brush.horizontalGradient(
                                        listOf(
                                            gradientStartColor.toComposeColor(),
                                            gradientEndColor.toComposeColor()
                                        )
                                    )
                                } else {
                                    SolidColor(editingColor.toComposeColor())
                                }
                            )
                    )
                    TextField(
                        value = hexValue,
                        onValueChange = { input ->
                            val sanitized = sanitizeHexInput(input)
                            hexValue = sanitized
                            hexToColorOrNull(sanitized)?.let { dispatchColor(it) }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.custom_theme_hex_code)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii
                        )
                    )
                    IconButton(onClick = {}, enabled = false) {
                        Icon(
                            imageVector = Icons.Rounded.Colorize,
                            contentDescription = stringResource(R.string.custom_theme_eyedropper_placeholder),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }
            }

            // Suggestion swatches
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.custom_theme_picker_suggestions),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CustomThemeColorPalette) { colorValue ->
                        PaletteSwatch(
                            colorValue = colorValue,
                            selected = colorFromHsv(localHue, localSaturation, localValue) == colorValue,
                            onClick = { dispatchColor(colorValue) }
                        )
                    }
                }
            }

            // Nút Apply
            Button(
                onClick = {
                    onApply(editingColor)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.apply), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GradientStopSelector(
    startColor: Long,
    endColor: Long,
    selectedStop: Int,
    onSelectedStopChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GradientStopChip(
            label = stringResource(R.string.custom_theme_gradient_start),
            color = startColor.toComposeColor(),
            selected = selectedStop == 0,
            onClick = { onSelectedStopChange(0) },
            modifier = Modifier.weight(1f)
        )
        GradientStopChip(
            label = stringResource(R.string.custom_theme_gradient_end),
            color = endColor.toComposeColor(),
            selected = selectedStop == 1,
            onClick = { onSelectedStopChange(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GradientStopChip(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (selected) 0.62f else 0.34f),
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

// ==========================================
// SATURATION/VALUE PICKER (HSV Square)
// ==========================================

@Composable
private fun SaturationValuePicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (Float, Float) -> Unit
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val hueColor = remember(hue) { colorFromHsv(hue, 1f, 1f).toComposeColor() }

    fun handleTouch(offset: Offset) {
        val width = canvasSize.width.coerceAtLeast(1).toFloat()
        val height = canvasSize.height.coerceAtLeast(1).toFloat()
        onChange(
            (offset.x / width).coerceIn(0f, 1f),
            1f - (offset.y / height).coerceIn(0f, 1f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(hueColor)
            .onSizeChanged { canvasSize = it }
            .pointerInput(hue) { detectTapGestures { handleTouch(it) } }
            .pointerInput(hue) {
                detectDragGestures(
                    onDragStart = { handleTouch(it) },
                    onDrag = { change, _ ->
                        handleTouch(change.position)
                        change.consume()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(brush = Brush.horizontalGradient(listOf(Color.White, Color.Transparent)))
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            val thumbX = saturation.coerceIn(0f, 1f) * size.width
            val thumbY = (1f - value.coerceIn(0f, 1f)) * size.height
            drawCircle(color = Color.White, center = Offset(thumbX, thumbY), radius = 12.dp.toPx(), style = Stroke(width = 3.dp.toPx()))
        }
    }
}

// ==========================================
// HUE SLIDER
// ==========================================

@Composable
private fun HueSlider(
    selectedHue: Float,
    onHueChange: (Float) -> Unit
) {
    var sliderSize by remember { mutableStateOf(IntSize.Zero) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(99.dp))
            .onSizeChanged { sliderSize = it }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val hue = (offset.x.coerceIn(0f, sliderSize.width.toFloat()) / sliderSize.width.coerceAtLeast(1).toFloat()) * 360f
                    onHueChange(hue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val hue = (offset.x.coerceIn(0f, sliderSize.width.toFloat()) / sliderSize.width.coerceAtLeast(1).toFloat()) * 360f
                        onHueChange(hue)
                    },
                    onDrag = { change, _ ->
                        val hue = (change.position.x.coerceIn(0f, sliderSize.width.toFloat()) / sliderSize.width.coerceAtLeast(1).toFloat()) * 360f
                        onHueChange(hue)
                        change.consume()
                    }
                )
            }
    ) {
        drawRoundRect(
            brush = Brush.horizontalGradient(RainbowSpectrumColors),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f, 999f)
        )
        val thumbX = (selectedHue / 360f).coerceIn(0f, 1f) * size.width
        drawCircle(color = Color.White, radius = 12.dp.toPx(), center = Offset(thumbX, size.height / 2f))
        drawCircle(color = colorFromHsv(selectedHue, 1f, 1f).toComposeColor(), radius = 8.dp.toPx(), center = Offset(thumbX, size.height / 2f))
    }
}

// ==========================================
// PALETTE SWATCH
// ==========================================

@Composable
private fun PaletteSwatch(
    colorValue: Long,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colorValue.toComposeColor())
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.78f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

// ==========================================
// BACKGROUND TRANSFORM DIALOG
// ==========================================

@Composable
private fun BackgroundTransformDialog(
    imageUri: String,
    onApply: (Float, Float, Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var flipH by remember { mutableStateOf(false) }
    var flipV by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 1f) // Đục hoàn toàn
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar: Cancel | Title | Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Text(
                        text = stringResource(R.string.custom_theme_edit_background),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    TextButton(onClick = { onApply(scale, rotation, offsetX, offsetY) }) {
                        Text(stringResource(R.string.apply), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hàng icon điều khiển: Rotate, FlipH, FlipV, Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rotate 90°
                    FilledTonalIconButton(onClick = { rotation = (rotation + 90f) % 360f }) {
                        Icon(Icons.Rounded.RotateRight, contentDescription = stringResource(R.string.content_desc_rotate_90))
                    }
                    Spacer(Modifier.width(8.dp))
                    // Flip ngang
                    FilledTonalIconButton(onClick = { flipH = !flipH }) {
                        Icon(Icons.Rounded.Flip, contentDescription = stringResource(R.string.content_desc_flip_horizontal))
                    }
                    Spacer(Modifier.width(8.dp))
                    // Flip dọc (icon flip xoay 90°)
                    FilledTonalIconButton(onClick = { flipV = !flipV }) {
                        Icon(
                            Icons.Rounded.Flip,
                            contentDescription = stringResource(R.string.content_desc_flip_vertical),
                            modifier = Modifier.graphicsLayer { rotationZ = 90f }
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    // Reset tất cả
                    OutlinedIconButton(onClick = {
                        scale = 1f; rotation = 0f; offsetX = 0f; offsetY = 0f
                        flipH = false; flipV = false
                    }) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = stringResource(R.string.content_desc_reset))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Image preview - hỗ trợ pinch-to-zoom + drag
                val safeScale = scale * rotationCoverMultiplier(rotation)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = safeScale * (if (flipH) -1f else 1f)
                                scaleY = safeScale * (if (flipV) -1f else 1f)
                                rotationZ = rotation
                                translationX = offsetX
                                translationY = offsetY
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, rotate ->
                                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                                    rotation += rotate
                                    offsetX += pan.x
                                    offsetY += pan.y
                                }
                            },
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hướng dẫn sử dụng
                Text(
                    text = stringResource(R.string.custom_theme_transform_guide),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

// ==========================================
// COLOR CONSTANTS & UTILITIES
// ==========================================

private val CustomThemeColorPalette = listOf(
    0xFF8D6E63L, 0xFFEF9A9AL, 0xFFFFCA28L, 0xFF81C784L, 0xFF64B5F6L, 0xFFBA68C8L,
    0xFF263238L, 0xFFFF8A65L, 0xFFAED581L, 0xFF4DB6ACL, 0xFF7986CBL, 0xFFF06292L,
    0xFFFFF7ECL, 0xFFE1F5FEL, 0xFFF3E5F5L, 0xFFE8F5E9L, 0xFFFFEBEEL, 0xFF212121L
)

private val RainbowSpectrumColors = listOf(
    Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00),
    Color(0xFF34C759), Color(0xFF00C7BE), Color(0xFF007AFF),
    Color(0xFF5856D6), Color(0xFFFF2D55)
)

private fun Long.toHsv(): FloatArray = FloatArray(3).also {
    android.graphics.Color.colorToHSV(this.toInt(), it)
}

private fun colorFromHsv(hue: Float, saturation: Float, value: Float): Long {
    return android.graphics.Color.HSVToColor(
        floatArrayOf(
            hue.coerceIn(0f, 360f),
            saturation.coerceIn(0f, 1f),
            value.coerceIn(0f, 1f)
        )
    ).toLong() and 0xFFFFFFFFL
}

private fun colorToHex(color: Long): String = String.format("#%06X", color.toInt() and 0x00FFFFFF)

private fun sanitizeHexInput(value: String): String {
    val raw = value.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }
    return "#${raw.take(6)}"
}

private fun hexToColorOrNull(value: String): Long? {
    val normalized = value.removePrefix("#")
    if (normalized.length != 6) return null
    return normalized.toLongOrNull(16)?.let { 0xFF000000L or it }
}

private data class PreviewProtectionColors(
    val contentColor: Color,
    val panelColor: Color,
    val bottomBarColor: Color,
    val cameraCutoutColor: Color
)

private fun previewProtectionColors(
    darkBackground: Boolean,
    hasVisualBackground: Boolean
): PreviewProtectionColors {
    return if (darkBackground) {
        PreviewProtectionColors(
            contentColor = Color(0xFFF7F2EA),
            panelColor = if (hasVisualBackground) Color(0xEE1E1E1E) else Color(0xFF151515),
            bottomBarColor = if (hasVisualBackground) Color(0xF0262626) else Color(0xFF424242),
            cameraCutoutColor = if (hasVisualBackground) Color(0xEE1E1E1E) else Color(0xFF1C1C1C)
        )
    } else {
        PreviewProtectionColors(
            contentColor = Color(0xFF2E261F),
            panelColor = if (hasVisualBackground) Color(0xF2FFFCF6) else Color.White,
            bottomBarColor = if (hasVisualBackground) Color(0xF5FFFCF6) else Color.White,
            cameraCutoutColor = if (hasVisualBackground) Color(0xF2FFFCF6) else Color(0xFFF4F6F1)
        )
    }
}

private fun previewImageScrim(isDarkMode: Boolean): Color =
    if (isDarkMode) Color.Black.copy(alpha = 0.58f) else Color.White.copy(alpha = 0.50f)

private fun rotationCoverMultiplier(rotation: Float): Float {
    val normalized = kotlin.math.abs(rotation) / 45f
    return 1f + normalized.coerceIn(0f, 1f) * 0.34f
}

private fun CustomThemeEditorUiState.isPreviewBackgroundDark(fallbackDark: Boolean): Boolean {
    if (!backgroundUri.isNullOrBlank()) return fallbackDark
    val brightness = when (backgroundFillMode) {
        BackgroundFillMode.Solid -> solidBackgroundColor.toComposeColor().perceivedBrightness()
        BackgroundFillMode.Gradient -> {
            val start = gradientStartColor.toComposeColor().perceivedBrightness()
            val end = gradientEndColor.toComposeColor().perceivedBrightness()
            (start + end) / 2f
        }
        BackgroundFillMode.Image -> return fallbackDark
    }
    return brightness < 0.5f
}

private fun Color.perceivedBrightness(): Float = red * 0.299f + green * 0.587f + blue * 0.114f

private fun Long.toComposeColor(): Color = Color(this.toInt())

private fun CustomThemeEditorUiState.hasBackgroundSelected(): Boolean {
    val isDefaultLightSolid = editingMode == EditorAppearanceMode.Light && backgroundFillMode == BackgroundFillMode.Solid && solidBackgroundColor == 0xFFFFF7EC
    val isDefaultDarkSolid = editingMode == EditorAppearanceMode.Dark && backgroundFillMode == BackgroundFillMode.Solid && solidBackgroundColor == 0xFF1C1C1CL
    return !backgroundUri.isNullOrBlank() || !(isDefaultLightSolid || isDefaultDarkSolid)
}
