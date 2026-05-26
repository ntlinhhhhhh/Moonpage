package com.diary.moonpage.ui.screens.store

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FormatColorReset
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
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
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonTheme
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost

private val EditorBottomToolsHeight = 280.dp
private val EditorBottomToolContentHeight = 200.dp

@Composable
fun CustomThemeEditorRoute(
    onNavigateBack: () -> Unit,
    viewModel: CustomThemeEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CustomThemeEditorEffect.Saved -> onNavigateBack()
                is CustomThemeEditorEffect.Error -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CustomThemeEditorScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNameChange = viewModel::updateName,
        onImagePicked = viewModel::setBackgroundUri,
        onApplyEditedImage = viewModel::applyPendingBackground,
        onCancelEditedImage = viewModel::cancelPendingBackground,
        onBackgroundFillModeSelected = viewModel::setBackgroundFillMode,
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
        onToolSelected = viewModel::setTool,
        onToggleEditingMode = viewModel::toggleEditingMode,
        onExitPreview = viewModel::exitPreview,
        onStrokeFinished = viewModel::addStroke,
        onUndo = viewModel::undoStroke,
        onClearStrokes = viewModel::clearStrokes,
        onBack = { viewModel.onBackRequested(onNavigateBack) },
        onDiscardDismiss = viewModel::dismissDiscardDialog,
        onDiscardConfirm = onNavigateBack,
        onSave = viewModel::saveTheme
    )
}

/*
 * Stateful route collects ViewModel state and effects. This stateless screen receives
 * immutable state plus event callbacks, following the state-hoisting pattern from
 * Google Android Basics so preview, tools, and dialogs remain independently testable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThemeEditorScreen(
    uiState: CustomThemeEditorUiState,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onImagePicked: (String?) -> Unit,
    onApplyEditedImage: (Float, Float, Float, Float) -> Unit,
    onCancelEditedImage: () -> Unit,
    onBackgroundFillModeSelected: (BackgroundFillMode) -> Unit,
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
    onToolSelected: (ThemeEditorTool) -> Unit,
    onToggleEditingMode: () -> Unit,
    onExitPreview: () -> Unit,
    onStrokeFinished: (DrawStroke) -> Unit,
    onUndo: () -> Unit,
    onClearStrokes: () -> Unit,
    onBack: () -> Unit,
    onDiscardDismiss: () -> Unit,
    onDiscardConfirm: () -> Unit,
    onSave: () -> Unit
) {
    var editorZoom by remember { mutableFloatStateOf(1f) }
    var editorPan by remember { mutableStateOf(Offset.Zero) }
    val previewTargetScale = when (uiState.selectedTool) {
        ThemeEditorTool.Background, ThemeEditorTool.Draw, ThemeEditorTool.Colors -> 0.82f
        ThemeEditorTool.Preview -> 1f
    }
    val animatedPreviewScale by animateFloatAsState(targetValue = previewTargetScale, label = "customThemePreviewScale")
    val animatedPreviewOffsetY by animateDpAsState(
        targetValue = if (uiState.selectedTool != ThemeEditorTool.Preview) (-12).dp else 0.dp,
        label = "customThemePreviewOffset"
    )
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        editorZoom = (editorZoom * zoomChange).coerceIn(1f, 2.8f)
        editorPan = if (editorZoom <= 1.02f) {
            Offset.Zero
        } else {
            Offset(
                x = (editorPan.x + panChange.x).coerceIn(-220f, 220f),
                y = (editorPan.y + panChange.y).coerceIn(-260f, 260f)
            )
        }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        onImagePicked(uri?.toString())
    }

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            if (uiState.selectedTool == ThemeEditorTool.Preview) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onExitPreview) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = stringResource(R.string.back))
                        }
                    },
                    actions = {
                        TextButton(
                            enabled = !uiState.isSaving,
                            modifier = Modifier.padding(end = 4.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            onClick = onSave
                        ) {
                            Text(stringResource(R.string.save), fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    title = {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = onNameChange,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.72f),
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = stringResource(R.string.back))
                        }
                    },
                    actions = {
                        TextButton(
                            enabled = !uiState.isSaving,
                            modifier = Modifier.padding(end = 4.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            onClick = onSave
                        ) {
                            Text(stringResource(R.string.save), fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (uiState.selectedTool != ThemeEditorTool.Preview) {
                ThemeEditorBottomTools(
                    uiState = uiState,
                    onToolSelected = onToolSelected,
                    onPickImage = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onBackgroundFillModeSelected = onBackgroundFillModeSelected,
                    onSolidBackgroundSelected = onSolidBackgroundSelected,
                    onGradientStartSelected = onGradientStartSelected,
                    onGradientEndSelected = onGradientEndSelected,
                    onPrimaryFocus = onPrimaryFocus,
                    onFocusedColorSelected = onFocusedColorSelected,
                    onIconSelected = onIconSelected,
                    onBrushColorSelected = onBrushColorSelected,
                    onBrushSizeChange = onBrushSizeChange,
                    onBrushTypeSelected = onBrushTypeSelected,
                    onEraserChanged = onEraserChanged,
                    onUndo = onUndo,
                    onClearStrokes = onClearStrokes
                )
            }
        },
        snackbarHost = { MoonSnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .offset(y = animatedPreviewOffsetY)
                                .graphicsLayer {
                                    scaleX = animatedPreviewScale * editorZoom
                                    scaleY = animatedPreviewScale * editorZoom
                                    translationX = editorPan.x
                                    translationY = editorPan.y
                                }
                                .transformable(transformState)
                        ) {
                            val previewMaxHeight = if (uiState.selectedTool == ThemeEditorTool.Preview) {
                                540.dp
                            } else {
                                minOf(492.dp, maxOf(360.dp, maxHeight - 28.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 360.dp)
                                    .heightIn(max = previewMaxHeight)
                                    .aspectRatio(0.62f)
                                    .clip(RoundedCornerShape(28.dp))
                            ) {
                                ThemePreviewCapture(
                                    uiState = uiState,
                                    showFullPreview = uiState.selectedTool == ThemeEditorTool.Preview,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(28.dp)),
                                    onPrimaryFocus = onPrimaryFocus,
                                    onIconSelected = onIconSelected,
                                    onLightModeSelected = {
                                        if (uiState.editingMode == EditorAppearanceMode.Dark) onToggleEditingMode()
                                    },
                                    onDarkModeSelected = {
                                        if (uiState.editingMode == EditorAppearanceMode.Light) onToggleEditingMode()
                                    },
                                    onToggleAppearance = if (uiState.selectedTool != ThemeEditorTool.Preview) onToggleEditingMode else null,
                                    onStrokeFinished = onStrokeFinished
                                )

                            }
                        }
                    }
                }

                if (uiState.isSaving) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.55f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    if (uiState.pendingBackgroundUri != null) {
        BackgroundTransformDialog(
            imageUri = uiState.pendingBackgroundUri,
            onApply = onApplyEditedImage,
            onDismiss = onCancelEditedImage
        )
    }

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
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

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
                if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier.background(solidBackground)
            )
            .then(
                if (uiState.selectedTool == ThemeEditorTool.Draw) {
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
            if (onToggleAppearance != null) {
                AppearanceModeFab(
                    isDarkMode = isDarkMode,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    onClick = onToggleAppearance
                )
            }

            when (uiState.selectedTool) {
                ThemeEditorTool.Background, ThemeEditorTool.Draw -> Unit
                ThemeEditorTool.Colors -> {
                    PreviewMoodIconRow(
                        colors = uiState.iconColors,
                        selectedIndex = if (uiState.colorFocusTarget == ColorFocusTarget.Icon) uiState.selectedIconIndex else -1,
                        onSelected = onIconSelected,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 84.dp)
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
                ThemeEditorTool.Preview -> Unit
            }
        }
    }
}

@Composable
private fun AppearanceModeFab(
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Semi-transparent frosted backdrop ensures icon is always visible regardless of background image brightness
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
    val calendarCardColor = panelColor

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = panelColor) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.padding(6.dp).size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.custom_theme_mock_month),
                        color = contentColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniModeIcon(
                        selected = !isDarkMode,
                        icon = Icons.Rounded.LightMode,
                        tint = primary,
                        panelColor = panelColor,
                        onClick = onLightModeSelected
                    )
                    MiniModeIcon(
                        selected = isDarkMode,
                        icon = Icons.Rounded.DarkMode,
                        tint = primary,
                        panelColor = panelColor,
                        onClick = onDarkModeSelected
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(28.dp),
                color = calendarCardColor,
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
                        panelColor = calendarCardColor,
                        transparentBackground = false,
                        modifier = Modifier.weight(1f)
                    )
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(7) { column ->
                        val day = row * 7 + column - 2
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
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
        Box(modifier = modifier.fillMaxWidth()) {
            content()
        }
    } else {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = panelColor,
            modifier = modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStroke(stroke: DrawStroke, background: Color) {
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
        color = if (stroke.isEraser) Color.Transparent else stroke.color.toComposeColor().copy(alpha = if (stroke.brushType == BrushType.Pencil) 0.68f else 1f),
        style = Stroke(width = width, cap = androidx.compose.ui.graphics.StrokeCap.Round),
        blendMode = if (stroke.isEraser) BlendMode.Clear else BlendMode.SrcOver
    )
}

@Composable
private fun ThemeEditorBottomTools(
    uiState: CustomThemeEditorUiState,
    onToolSelected: (ThemeEditorTool) -> Unit,
    onPickImage: () -> Unit,
    onBackgroundFillModeSelected: (BackgroundFillMode) -> Unit,
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
    onUndo: () -> Unit,
    onClearStrokes: () -> Unit
) {
    // FIX PHẦN 1: Fixed-height container so the preview above NEVER shifts
    // FIX PHẦN 2.4: Rounded top corners + shadow to visually separate tool panel from the app's bottom nav bar
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(EditorBottomToolsHeight),
        tonalElevation = 0.dp,
        shadowElevation = 20.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MoonTheme.customColors.popupBgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // FIX PHẦN 2.1: Transparent TabRow — 4 icons float naturally on the app background
            TabRow(
                selectedTabIndex = uiState.selectedTool.ordinal,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                ThemeEditorTool.entries.forEach { tool ->
                    Tab(
                        selected = uiState.selectedTool == tool,
                        onClick = { onToolSelected(tool) },
                        icon = {
                            Icon(
                                imageVector = when (tool) {
                                    ThemeEditorTool.Background -> Icons.Rounded.Image
                                    ThemeEditorTool.Draw -> Icons.Rounded.Brush
                                    ThemeEditorTool.Colors -> Icons.Rounded.Palette
                                    ThemeEditorTool.Preview -> Icons.Rounded.Visibility
                                },
                                contentDescription = when (tool) {
                                    ThemeEditorTool.Background -> stringResource(R.string.custom_theme_tool_background)
                                    ThemeEditorTool.Draw -> stringResource(R.string.custom_theme_tool_draw)
                                    ThemeEditorTool.Colors -> stringResource(R.string.custom_theme_tool_colors)
                                    ThemeEditorTool.Preview -> stringResource(R.string.custom_theme_tool_preview)
                                }
                            )
                        }
                    )
                }
            }

            // FIX PHẦN 1 & 2.1: Fixed-height content area with AnimatedVisibility.
            // The 4-icon tab row stays fixed; only the tool content area animates in/out.
            // Height is locked so the Preview above never moves.
            val showContent = uiState.selectedTool != ThemeEditorTool.Preview
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(EditorBottomToolContentHeight)
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp, top = 4.dp)
                ) {
                    // FIX PHẦN 1: verticalScroll inside fixed area handles overflow (e.g. Background tab with many sliders)
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        when (uiState.selectedTool) {
                            ThemeEditorTool.Background -> BackgroundToolPanel(
                                uiState = uiState,
                                onPickImage = onPickImage,
                                onBackgroundFillModeSelected = onBackgroundFillModeSelected,
                                onSolidBackgroundSelected = onSolidBackgroundSelected,
                                onGradientStartSelected = onGradientStartSelected,
                                onGradientEndSelected = onGradientEndSelected
                            )
                            ThemeEditorTool.Draw -> DrawToolPanel(
                                uiState = uiState,
                                onBrushColorSelected = onBrushColorSelected,
                                onBrushSizeChange = onBrushSizeChange,
                                onBrushTypeSelected = onBrushTypeSelected,
                                onEraserChanged = onEraserChanged,
                                onUndo = onUndo,
                                onClearStrokes = onClearStrokes
                            )
                            ThemeEditorTool.Colors -> ColorToolPanel(
                                uiState = uiState,
                                onPrimaryFocus = onPrimaryFocus,
                                onIconSelected = onIconSelected,
                                onFocusedColorSelected = onFocusedColorSelected
                            )
                            ThemeEditorTool.Preview -> Unit
                        }
                    }
                }
            }
        }
    }
}

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
            .shadow(elevation = 16.dp, spotColor = Color.Black.copy(alpha = 0.10f))
            .clickable(enabled = onPrimarySelected != null) { onPrimarySelected?.invoke() },
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MockNavIcon(Icons.Rounded.CalendarMonth, tint = primary, emphasized = emphasizeIcons)
            MockNavIcon(Icons.Rounded.BarChart, tint = primary, emphasized = emphasizeIcons)
            Box(
                modifier = Modifier
                    .size(if (emphasizeIcons) 60.dp else 56.dp)
                    .clip(CircleShape)
                    .background(centerCutoutColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(if (emphasizeIcons) 30.dp else 28.dp)
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
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(if (emphasized) 40.dp else 32.dp)
            .clip(CircleShape)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(if (emphasized) 32.dp else 26.dp)
        )
    }
}

@Composable
private fun BackgroundToolPanel(
    uiState: CustomThemeEditorUiState,
    onPickImage: () -> Unit,
    onBackgroundFillModeSelected: (BackgroundFillMode) -> Unit,
    onSolidBackgroundSelected: (Long) -> Unit,
    onGradientStartSelected: (Long) -> Unit,
    onGradientEndSelected: (Long) -> Unit
) {
    var showColorSheet by remember { mutableStateOf(false) }
    var selectedGradientStop by remember { mutableIntStateOf(0) }
    val activeBackgroundColor = if (uiState.backgroundFillMode == BackgroundFillMode.Gradient) {
        if (selectedGradientStop == 0) uiState.gradientStartColor else uiState.gradientEndColor
    } else {
        uiState.solidBackgroundColor
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledTonalButton(
            onClick = onPickImage,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.custom_theme_upload_image), fontWeight = FontWeight.SemiBold)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.backgroundFillMode == BackgroundFillMode.Solid,
                onClick = { onBackgroundFillModeSelected(BackgroundFillMode.Solid) },
                label = { Text(stringResource(R.string.custom_theme_solid_color)) },
                colors = customThemeFilterChipColors()
            )
            FilterChip(
                selected = uiState.backgroundFillMode == BackgroundFillMode.Gradient,
                onClick = { onBackgroundFillModeSelected(BackgroundFillMode.Gradient) },
                label = { Text(stringResource(R.string.custom_theme_gradient_color)) },
                colors = customThemeFilterChipColors()
            )
        }

        if (uiState.backgroundFillMode == BackgroundFillMode.Gradient) {
            GradientStopSelector(
                startColor = uiState.gradientStartColor,
                endColor = uiState.gradientEndColor,
                selectedStop = selectedGradientStop,
                onSelectedStopChange = { selectedGradientStop = it }
            )
        }

        PaletteSwatchRow(
            selected = activeBackgroundColor,
            onSelected = { color ->
                if (uiState.backgroundFillMode == BackgroundFillMode.Gradient) {
                    if (selectedGradientStop == 0) onGradientStartSelected(color) else onGradientEndSelected(color)
                } else {
                    onSolidBackgroundSelected(color)
                }
            },
            onAddColor = { showColorSheet = true }
        )
    }

    if (showColorSheet) {
        ColorPickerBottomSheet(
            selectedColor = uiState.solidBackgroundColor,
            onColorSelected = onSolidBackgroundSelected,
            allowGradient = true,
            mode = uiState.backgroundFillMode,
            onModeChange = onBackgroundFillModeSelected,
            gradientStartColor = uiState.gradientStartColor,
            gradientEndColor = uiState.gradientEndColor,
            activeGradientStop = selectedGradientStop,
            onActiveGradientStopChange = { selectedGradientStop = it },
            onGradientStartColorSelected = onGradientStartSelected,
            onGradientEndColorSelected = onGradientEndSelected,
            onDismiss = { showColorSheet = false }
        )
    }
}

@Composable
private fun DrawToolPanel(
    uiState: CustomThemeEditorUiState,
    onBrushColorSelected: (Long) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onBrushTypeSelected: (BrushType) -> Unit,
    onEraserChanged: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onClearStrokes: () -> Unit
) {
    var showColorSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onUndo, enabled = uiState.strokes.isNotEmpty()) {
            Icon(
                Icons.AutoMirrored.Rounded.Undo,
                contentDescription = stringResource(R.string.undo),
                tint = if (uiState.strokes.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
            )
        }
        IconButton(onClick = onClearStrokes, enabled = uiState.strokes.isNotEmpty()) {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.custom_theme_clear_strokes),
                tint = if (uiState.strokes.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        BrushType.entries.forEach { type ->
            FilterChip(
                selected = !uiState.isEraser && uiState.brushType == type,
                onClick = {
                    if (uiState.isEraser) onEraserChanged(false)
                    onBrushTypeSelected(type)
                },
                colors = customThemeFilterChipColors(),
                label = {
                    Text(
                        when (type) {
                            BrushType.Fine -> stringResource(R.string.custom_theme_brush_fine)
                            BrushType.Bold -> stringResource(R.string.custom_theme_brush_bold)
                            BrushType.Pencil -> stringResource(R.string.custom_theme_brush_pencil)
                            BrushType.Spray -> stringResource(R.string.custom_theme_brush_spray)
                        }
                    )
                }
            )
        }
        FilterChip(
            selected = uiState.isEraser,
            onClick = { onEraserChanged(true) },
            label = { Text(stringResource(R.string.custom_theme_eraser)) },
            leadingIcon = { Icon(Icons.Rounded.FormatColorReset, contentDescription = null, modifier = Modifier.size(18.dp)) },
            colors = customThemeFilterChipColors()
        )
    }
    PaletteSwatchRow(
        selected = uiState.brushColor,
        onSelected = onBrushColorSelected,
        onAddColor = { showColorSheet = true }
    )
    Slider(value = uiState.brushSize, onValueChange = onBrushSizeChange, valueRange = 3f..24f)

    if (showColorSheet) {
        ColorPickerBottomSheet(
            selectedColor = uiState.brushColor,
            onColorSelected = onBrushColorSelected,
            onDismiss = { showColorSheet = false }
        )
    }
}

@Composable
private fun ColorToolPanel(
    uiState: CustomThemeEditorUiState,
    onPrimaryFocus: () -> Unit,
    onIconSelected: (Int) -> Unit,
    onFocusedColorSelected: (Long) -> Unit,
) {
    var showColorSheet by remember { mutableStateOf(false) }
    val focusedColor = when (uiState.colorFocusTarget) {
        ColorFocusTarget.Primary -> uiState.primaryColor
        ColorFocusTarget.Icon -> uiState.iconColors[uiState.selectedIconIndex]
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.colorFocusTarget == ColorFocusTarget.Primary,
                onClick = onPrimaryFocus,
                label = { Text(stringResource(R.string.custom_theme_primary_color)) },
                colors = customThemeFilterChipColors()
            )
            FilterChip(
                selected = uiState.colorFocusTarget == ColorFocusTarget.Icon,
                onClick = { onIconSelected(uiState.selectedIconIndex) },
                label = { Text(stringResource(R.string.custom_theme_icon_color)) },
                colors = customThemeFilterChipColors()
            )
        }
        PreviewMoodIconRow(
            colors = uiState.iconColors,
            selectedIndex = if (uiState.colorFocusTarget == ColorFocusTarget.Icon) uiState.selectedIconIndex else -1,
            onSelected = onIconSelected,
            modifier = Modifier.fillMaxWidth()
        )
        PaletteSwatchRow(
            selected = focusedColor,
            onSelected = onFocusedColorSelected,
            onAddColor = { showColorSheet = true }
        )
    }

    if (showColorSheet) {
        ColorPickerBottomSheet(
            selectedColor = focusedColor,
            onColorSelected = onFocusedColorSelected,
            onDismiss = { showColorSheet = false }
        )
    }
}

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
            MoodFaceIcon(
                index = index,
                color = color.toComposeColor(),
                modifier = Modifier
                    .size(if (selected) 58.dp else 46.dp)
                    .scale(if (selected) 1.08f else 1f)
                    .shadow(if (selected) 10.dp else 0.dp, CircleShape)
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
        modifier = modifier.clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(drawable),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.58f)
        )
    }
}

@Composable
private fun PreviewBottomIconRow(primary: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
        Icon(Icons.Rounded.BarChart, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
        Icon(Icons.Rounded.Add, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
        Icon(Icons.Rounded.Storefront, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
        Icon(Icons.Rounded.Person, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun AddNewColorButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val rainbowBrush = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFFFF4D4D),
                Color(0xFFFFB84D),
                Color(0xFFFFF176),
                Color(0xFF66BB6A),
                Color(0xFF4FC3F7),
                Color(0xFF7E57C2),
                Color(0xFFFF4D4D)
            )
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .background(rainbowBrush, CircleShape)
            .padding(2.5.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.custom_theme_add_color),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(size * 0.42f)
        )
    }
}

@Composable
private fun customThemeFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
    labelColor = MaterialTheme.colorScheme.onSurface,
    iconColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun PaletteSwatchRow(
    selected: Long,
    onSelected: (Long) -> Unit,
    onAddColor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customSelectedColor = selected.takeIf { it !in CustomThemeColorPalette }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
    ) {
        item {
            AddNewColorButton(onClick = onAddColor)
        }
        if (customSelectedColor != null) {
            item {
                PaletteSwatch(
                    colorValue = customSelectedColor,
                    selected = true,
                    onClick = { onSelected(customSelectedColor) }
                )
            }
        }
        items(CustomThemeColorPalette) { colorValue ->
            PaletteSwatch(
                colorValue = colorValue,
                selected = colorValue == selected,
                onClick = { onSelected(colorValue) }
            )
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorPickerBottomSheet(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
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
            if (activeGradientStop == 0) onGradientStartColorSelected(color) else onGradientEndColorSelected(color)
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
                    dispatchColor(colorFromHsv(hue, localSaturation.coerceAtLeast(0.01f), localValue.coerceAtLeast(0.01f)))
                }
            )

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
        val normalizedX = (offset.x / width).coerceIn(0f, 1f)
        val normalizedY = (offset.y / height).coerceIn(0f, 1f)
        onChange(normalizedX, 1f - normalizedY)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(hueColor)
            .onSizeChanged { canvasSize = it }
            .pointerInput(hue) {
                detectTapGestures { handleTouch(it) }
            }
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
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.White, Color.Transparent)
                )
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black)
                )
            )
            val thumbX = saturation.coerceIn(0f, 1f) * size.width
            val thumbY = (1f - value.coerceIn(0f, 1f)) * size.height
            drawCircle(
                color = Color.Transparent,
                center = Offset(thumbX, thumbY),
                radius = 12.dp.toPx(),
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                center = Offset(thumbX, thumbY),
                radius = 12.dp.toPx(),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
private fun HueSlider(
    selectedHue: Float,
    onHueChange: (Float) -> Unit
) {
    var sliderSize by remember { mutableStateOf(IntSize.Zero) }

    fun handleTouch(offset: Offset) {
        val width = sliderSize.width.coerceAtLeast(1).toFloat()
        val hue = (offset.x.coerceIn(0f, width) / width) * 360f
        onHueChange(hue)
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(99.dp))
            .onSizeChanged { sliderSize = it }
            .pointerInput(Unit) {
                detectTapGestures { handleTouch(it) }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { handleTouch(it) },
                    onDrag = { change, _ ->
                        handleTouch(change.position)
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
        drawCircle(
            color = Color.White,
            radius = 12.dp.toPx(),
            center = Offset(thumbX, size.height / 2f)
        )
        drawCircle(
            color = colorFromHsv(selectedHue, 1f, 1f).toComposeColor(),
            radius = 8.dp.toPx(),
            center = Offset(thumbX, size.height / 2f)
        )
    }
}

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                Spacer(modifier = Modifier.height(16.dp))

                val safeScale = scale * rotationCoverMultiplier(rotation)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = safeScale
                                scaleY = safeScale
                                rotationZ = rotation
                                translationX = offsetX
                                translationY = offsetY
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                    change.consume()
                                }
                            },
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // FIX PHẦN 2.5: Generous bottom padding (48.dp) so fingers don't accidentally
                // trigger the Android system home/navigation gesture while dragging sliders
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.custom_theme_zoom),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Slider(scale, onValueChange = { scale = it }, valueRange = 1f..3f)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.custom_theme_rotate),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Slider(rotation, onValueChange = { rotation = it }, valueRange = -45f..45f)
                }
            }
        }
    }
}

private val CustomThemeColorPalette = listOf(
    0xFF8D6E63, 0xFFEF9A9A, 0xFFFFCA28, 0xFF81C784, 0xFF64B5F6, 0xFFBA68C8,
    0xFF263238, 0xFFFF8A65, 0xFFAED581, 0xFF4DB6AC, 0xFF7986CB, 0xFFF06292,
    0xFFFFF7EC, 0xFFE1F5FE, 0xFFF3E5F5, 0xFFE8F5E9, 0xFFFFEBEE, 0xFF212121
)

private val RainbowSpectrumColors = listOf(
    Color(0xFFFF3B30),
    Color(0xFFFF9500),
    Color(0xFFFFCC00),
    Color(0xFF34C759),
    Color(0xFF00C7BE),
    Color(0xFF007AFF),
    Color(0xFF5856D6),
    Color(0xFFFF2D55)
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

private fun colorToHex(color: Long): String {
    return String.format("#%06X", color.toInt() and 0x00FFFFFF)
}

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

private fun previewImageScrim(isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        Color.Black.copy(alpha = 0.58f)
    } else {
        Color.White.copy(alpha = 0.50f)
    }
}

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
    }
    return brightness < 0.5f
}

private fun Color.perceivedBrightness(): Float {
    return red * 0.299f + green * 0.587f + blue * 0.114f
}

private fun Long.toComposeColor(): Color = Color(this.toInt())
