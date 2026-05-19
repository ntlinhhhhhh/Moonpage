package com.diary.moonpage.ui.screens.store

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DarkMode
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonTheme
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import kotlinx.coroutines.launch

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
        onSolidBackgroundSelected = viewModel::setSolidBackgroundColor,
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
    onSolidBackgroundSelected: (Long) -> Unit,
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
    onBack: () -> Unit,
    onDiscardDismiss: () -> Unit,
    onDiscardConfirm: () -> Unit,
    onSave: (android.graphics.Bitmap) -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    var editorZoom by remember { mutableFloatStateOf(1f) }
    var editorPan by remember { mutableStateOf(Offset.Zero) }
    val previewTargetScale = when (uiState.selectedTool) {
        ThemeEditorTool.Draw, ThemeEditorTool.Colors -> 0.84f
        ThemeEditorTool.Preview -> 1f
        ThemeEditorTool.Background -> 1f
    }
    val animatedPreviewScale by animateFloatAsState(targetValue = previewTargetScale, label = "customThemePreviewScale")
    val animatedPreviewOffsetY by animateDpAsState(
        targetValue = if (uiState.selectedTool == ThemeEditorTool.Draw || uiState.selectedTool == ThemeEditorTool.Colors) (-18).dp else 0.dp,
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
                            onClick = {
                                scope.launch {
                                    onSave(graphicsLayer.toImageBitmap().asAndroidBitmap())
                                }
                            }
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
                            onClick = {
                                scope.launch {
                                    onSave(graphicsLayer.toImageBitmap().asAndroidBitmap())
                                }
                            }
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
                    onSolidBackgroundSelected = onSolidBackgroundSelected,
                    onPrimaryFocus = onPrimaryFocus,
                    onFocusedColorSelected = onFocusedColorSelected,
                    onIconSelected = onIconSelected,
                    onBrushColorSelected = onBrushColorSelected,
                    onBrushSizeChange = onBrushSizeChange,
                    onBrushTypeSelected = onBrushTypeSelected,
                    onEraserChanged = onEraserChanged,
                    onUndo = onUndo
                )
            }
        },
        snackbarHost = { MoonSnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                    Box(
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
                        ThemePreviewCapture(
                            uiState = uiState,
                            showFullPreview = uiState.selectedTool == ThemeEditorTool.Preview,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 360.dp)
                                .heightIn(max = 540.dp)
                                .aspectRatio(0.62f)
                                .clip(RoundedCornerShape(28.dp))
                                .drawWithContent {
                                    graphicsLayer.record {
                                        this@drawWithContent.drawContent()
                                    }
                                    drawLayer(graphicsLayer)
                                },
                            onPrimaryFocus = onPrimaryFocus,
                            onIconSelected = onIconSelected,
                            onLightModeSelected = {
                                if (uiState.editingMode == EditorAppearanceMode.Dark) onToggleEditingMode()
                            },
                            onDarkModeSelected = {
                                if (uiState.editingMode == EditorAppearanceMode.Light) onToggleEditingMode()
                            },
                            onStrokeFinished = onStrokeFinished
                        )
                        if (uiState.selectedTool != ThemeEditorTool.Preview) {
                            AppearanceModeFab(
                                isDarkMode = uiState.editingMode == EditorAppearanceMode.Dark,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp),
                                onClick = onToggleEditingMode
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
                TextButton(onClick = onDiscardDismiss) {
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
    onStrokeFinished: (DrawStroke) -> Unit
) {
    var activePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val primary = uiState.primaryColor.toComposeColor()
    val background = uiState.solidBackgroundColor.toComposeColor()
    val isDarkMode = uiState.editingMode == EditorAppearanceMode.Dark
    val contentColor = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface
    val panelColor = if (isDarkMode) Color(0xFF151515).copy(alpha = 0.72f) else Color.White.copy(alpha = 0.66f)
    val safeBackgroundScale = uiState.backgroundScale * rotationCoverMultiplier(uiState.backgroundRotation)

    Box(
        modifier = modifier
            .background(background)
            .then(
                if (uiState.selectedTool == ThemeEditorTool.Draw) {
                    Modifier.pointerInput(uiState.brushColor, uiState.brushSize, uiState.brushType, uiState.isEraser) {
                        detectDragGestures(
                            onDragStart = { activePoints = listOf(it) },
                            onDrag = { change, _ ->
                                activePoints = activePoints + change.position
                                change.consume()
                            },
                            onDragEnd = {
                                onStrokeFinished(
                                    DrawStroke(
                                        points = activePoints,
                                        color = uiState.brushColor,
                                        strokeWidth = uiState.brushSize,
                                        brushType = uiState.brushType,
                                        isEraser = uiState.isEraser
                                    )
                                )
                                activePoints = emptyList()
                            },
                            onDragCancel = { activePoints = emptyList() }
                        )
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
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            uiState.strokes.forEach { drawStroke(it, background) }
            if (activePoints.size > 1) {
                drawStroke(
                    DrawStroke(
                        points = activePoints,
                        color = uiState.brushColor,
                        strokeWidth = uiState.brushSize,
                        brushType = uiState.brushType,
                        isEraser = uiState.isEraser
                    ),
                    background
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (showFullPreview) {
                ThemeCalendarMockScreen(
                    isDarkMode = isDarkMode,
                    primary = primary,
                    iconColors = uiState.iconColors,
                    contentColor = contentColor,
                    panelColor = panelColor,
                    onLightModeSelected = onLightModeSelected,
                    onDarkModeSelected = onDarkModeSelected
                )
            }

            if (!showFullPreview) {
                when (uiState.selectedTool) {
                    ThemeEditorTool.Background -> Unit
                    ThemeEditorTool.Draw -> Unit
                    ThemeEditorTool.Colors -> Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        PreviewMoodIconRow(
                            colors = uiState.iconColors,
                            selectedIndex = if (uiState.colorFocusTarget == ColorFocusTarget.Icon) uiState.selectedIconIndex else -1,
                            onSelected = onIconSelected,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        MockAppBottomNavBar(
                            primary = primary,
                            onPrimarySelected = onPrimaryFocus
                        )
                    }
                    ThemeEditorTool.Preview -> Unit
                }
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
    Surface(
        modifier = modifier,
        shape = CircleShape,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                contentDescription = stringResource(if (isDarkMode) R.string.dark else R.string.light),
                tint = MaterialTheme.colorScheme.primary
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
    onLightModeSelected: () -> Unit,
    onDarkModeSelected: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
                    color = primary,
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

        MockCalendarWeekHeader(contentColor = contentColor)
        MockCalendarGrid(
            primary = primary,
            iconColors = iconColors,
            contentColor = contentColor,
            panelColor = panelColor,
            modifier = Modifier.weight(1f)
        )

        PreviewMoodIconRow(
            colors = iconColors,
            selectedIndex = -1,
            onSelected = {},
        )

        MockAppBottomNavBar(primary = primary)
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
    modifier: Modifier = Modifier
) {
    val moodDays = mapOf(2 to 0, 5 to 1, 9 to 2, 14 to 3, 18 to 4, 22 to 0, 27 to 1)
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = panelColor,
        modifier = modifier.fillMaxWidth()
    ) {
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
                                        color = if (moodIndex != null) primary else contentColor.copy(alpha = 0.55f),
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
    onSolidBackgroundSelected: (Long) -> Unit,
    onPrimaryFocus: () -> Unit,
    onFocusedColorSelected: (Long) -> Unit,
    onIconSelected: (Int) -> Unit,
    onBrushColorSelected: (Long) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onBrushTypeSelected: (BrushType) -> Unit,
    onEraserChanged: (Boolean) -> Unit,
    onUndo: () -> Unit
) {
    Surface(tonalElevation = 8.dp, color = MoonTheme.customColors.popupBgColor) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(12.dp)) {
            TabRow(selectedTabIndex = uiState.selectedTool.ordinal) {
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

            Spacer(modifier = Modifier.height(12.dp))

            when (uiState.selectedTool) {
                ThemeEditorTool.Background -> BackgroundToolPanel(
                    selected = uiState.solidBackgroundColor,
                    onPickImage = onPickImage,
                    onSolidBackgroundSelected = onSolidBackgroundSelected
                )
                ThemeEditorTool.Draw -> DrawToolPanel(
                    uiState = uiState,
                    onBrushColorSelected = onBrushColorSelected,
                    onBrushSizeChange = onBrushSizeChange,
                    onBrushTypeSelected = onBrushTypeSelected,
                    onEraserChanged = onEraserChanged,
                    onUndo = onUndo
                )
                ThemeEditorTool.Colors -> ColorToolPanel(
                    uiState = uiState,
                    onFocusedColorSelected = onFocusedColorSelected
                )
                ThemeEditorTool.Preview -> Unit
            }
        }
    }
}

@Composable
private fun MockAppBottomNavBar(
    primary: Color,
    onPrimarySelected: (() -> Unit)? = null
) {
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, spotColor = Color.Black.copy(alpha = 0.10f)),
        color = MoonTheme.customColors.bottomNavBg
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 6.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MockNavIcon(Icons.Rounded.CalendarMonth, tint = primary, onClick = onPrimarySelected)
            MockNavIcon(Icons.Rounded.BarChart, tint = inactiveColor, onClick = onPrimarySelected)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    tint = inactiveColor,
                    modifier = Modifier.size(28.dp).clickable(enabled = onPrimarySelected != null) { onPrimarySelected?.invoke() }
                )
            }
            MockNavIcon(Icons.Rounded.Storefront, tint = inactiveColor, onClick = onPrimarySelected)
            MockNavIcon(Icons.Rounded.Person, tint = inactiveColor, onClick = onPrimarySelected)
        }
    }
}

@Composable
private fun MockNavIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: (() -> Unit)? = null
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .padding(8.dp)
            .size(28.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    )
}

@Composable
private fun BackgroundToolPanel(
    selected: Long,
    onPickImage: () -> Unit,
    onSolidBackgroundSelected: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onPickImage, shape = RoundedCornerShape(12.dp)) {
            Text(stringResource(R.string.custom_theme_upload_image))
        }
        ExtendedColorSwatches(selected = selected, onSelected = onSolidBackgroundSelected)
    }
}

@Composable
private fun DrawToolPanel(
    uiState: CustomThemeEditorUiState,
    onBrushColorSelected: (Long) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onBrushTypeSelected: (BrushType) -> Unit,
    onEraserChanged: (Boolean) -> Unit,
    onUndo: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onUndo, enabled = uiState.strokes.isNotEmpty()) {
            Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = stringResource(R.string.undo))
        }
        FilterChip(
            selected = uiState.isEraser,
            onClick = { onEraserChanged(!uiState.isEraser) },
            label = { Text(stringResource(R.string.custom_theme_eraser)) },
            leadingIcon = { Icon(Icons.Rounded.FormatColorReset, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        BrushType.entries.forEach { type ->
            FilterChip(
                selected = uiState.brushType == type,
                onClick = { onBrushTypeSelected(type) },
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
    }
    ExtendedColorSwatches(selected = uiState.brushColor, onSelected = onBrushColorSelected)
    Slider(value = uiState.brushSize, onValueChange = onBrushSizeChange, valueRange = 3f..24f)
}

@Composable
private fun ColorToolPanel(
    uiState: CustomThemeEditorUiState,
    onFocusedColorSelected: (Long) -> Unit,
) {
    val focusedColor = when (uiState.colorFocusTarget) {
        ColorFocusTarget.Primary -> uiState.primaryColor
        ColorFocusTarget.Icon -> uiState.iconColors[uiState.selectedIconIndex]
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FreeColorPicker2D(
            selected = focusedColor,
            onSelected = onFocusedColorSelected
        )
        CompactDefaultColorSwatches(selected = focusedColor, onSelected = onFocusedColorSelected)
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
        Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
        Icon(Icons.Rounded.Storefront, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
        Icon(Icons.Rounded.Person, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun FreeColorPicker2D(
    selected: Long,
    onSelected: (Long) -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    fun colorAt(offset: Offset): Long {
        val width = size.width.coerceAtLeast(1).toFloat()
        val height = size.height.coerceAtLeast(1).toFloat()
        val hue = (offset.x.coerceIn(0f, width) / width) * 360f
        val value = 1f - (offset.y.coerceIn(0f, height) / height) * 0.82f
        return android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.86f, value)).toLong() and 0xFFFFFFFFL
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(18.dp))
            .onSizeChanged { size = it }
            .pointerInput(size) {
                detectTapGestures { onSelected(colorAt(it)) }
            }
            .pointerInput(size) {
                detectDragGestures(
                    onDragStart = { onSelected(colorAt(it)) },
                    onDrag = { change, _ ->
                        onSelected(colorAt(change.position))
                        change.consume()
                    }
                )
            }
    ) {
        val columns = 48
        val rows = 18
        val cellWidth = this.size.width / columns
        val cellHeight = this.size.height / rows
        repeat(columns) { x ->
            repeat(rows) { y ->
                val hue = (x.toFloat() / (columns - 1)) * 360f
                val value = 1f - (y.toFloat() / (rows - 1)) * 0.82f
                drawRect(
                    color = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.86f, value))),
                    topLeft = Offset(x * cellWidth, y * cellHeight),
                    size = androidx.compose.ui.geometry.Size(cellWidth + 1f, cellHeight + 1f)
                )
            }
        }
        drawCircle(
            color = Color.White,
            radius = 9.dp.toPx(),
            center = Offset(this.size.width - 20.dp.toPx(), 20.dp.toPx())
        )
        drawCircle(
            color = selected.toComposeColor(),
            radius = 7.dp.toPx(),
            center = Offset(this.size.width - 20.dp.toPx(), 20.dp.toPx())
        )
    }
}

@Composable
private fun CompactDefaultColorSwatches(
    selected: Long,
    onSelected: (Long) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
    ) {
        CustomThemeColorPalette.forEach { colorValue ->
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(colorValue.toComposeColor())
                    .border(
                        width = if (selected == colorValue) 3.dp else 1.dp,
                        color = if (selected == colorValue) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.75f),
                        shape = CircleShape
                    )
                    .clickable { onSelected(colorValue) }
            )
        }
    }
}

@Composable
private fun ExtendedColorSwatches(
    selected: Long,
    onSelected: (Long) -> Unit
) {
    var showFullPalette by remember { mutableStateOf(false) }
    val colors = CustomThemeColorPalette
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState())
        ) {
            colors.forEach { colorValue ->
                val color = colorValue.toComposeColor()
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selected == colorValue) 3.dp else 1.dp,
                            color = if (selected == colorValue) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.75f),
                            shape = CircleShape
                        )
                        .clickable { onSelected(colorValue) }
                )
            }
        }
        OutlinedButton(onClick = { showFullPalette = true }, shape = RoundedCornerShape(12.dp)) {
            Text(stringResource(R.string.custom_theme_pick_color))
        }
    }
    if (showFullPalette) {
        FullColorPaletteDialog(
            selected = selected,
            onSelected = {
                onSelected(it)
                showFullPalette = false
            },
            onDismiss = { showFullPalette = false }
        )
    }
}

@Composable
private fun FullColorPaletteDialog(
    selected: Long,
    onSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember(selected) { mutableFloatStateOf(android.graphics.Color.red(selected.toInt()).toFloat()) }
    var green by remember(selected) { mutableFloatStateOf(android.graphics.Color.green(selected.toInt()).toFloat()) }
    var blue by remember(selected) { mutableFloatStateOf(android.graphics.Color.blue(selected.toInt()).toFloat()) }
    val customColor = Color(red.toInt(), green.toInt(), blue.toInt())
    val customValue = (0xFF000000L or (red.toInt().toLong() shl 16) or (green.toInt().toLong() shl 8) or blue.toInt().toLong())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_theme_pick_color)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CustomThemeColorPalette.chunked(6).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { value ->
                            val color = value.toComposeColor()
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(if (selected == value) 3.dp else 1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    .clickable { onSelected(value) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(customColor)
                )
                Text(stringResource(R.string.custom_theme_red_value, red.toInt()), style = MaterialTheme.typography.labelSmall)
                Slider(value = red, onValueChange = { red = it }, valueRange = 0f..255f)
                Text(stringResource(R.string.custom_theme_green_value, green.toInt()), style = MaterialTheme.typography.labelSmall)
                Slider(value = green, onValueChange = { green = it }, valueRange = 0f..255f)
                Text(stringResource(R.string.custom_theme_blue_value, blue.toInt()), style = MaterialTheme.typography.labelSmall)
                Slider(value = blue, onValueChange = { blue = it }, valueRange = 0f..255f)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelected(customValue) }) { Text(stringResource(R.string.apply)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_theme_edit_background)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val safeScale = scale * rotationCoverMultiplier(rotation)
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(0.62f).clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.08f)),
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
                Text(stringResource(R.string.custom_theme_zoom))
                Slider(scale, onValueChange = { scale = it }, valueRange = 1f..3f)
                Text(stringResource(R.string.custom_theme_rotate))
                Slider(rotation, onValueChange = { rotation = it }, valueRange = -45f..45f)
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(scale, rotation, offsetX, offsetY) }) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private val CustomThemeColorPalette = listOf(
    0xFF8D6E63, 0xFFEF9A9A, 0xFFFFCA28, 0xFF81C784, 0xFF64B5F6, 0xFFBA68C8,
    0xFF263238, 0xFFFF8A65, 0xFFAED581, 0xFF4DB6AC, 0xFF7986CB, 0xFFF06292,
    0xFFFFF7EC, 0xFFE1F5FE, 0xFFF3E5F5, 0xFFE8F5E9, 0xFFFFEBEE, 0xFF212121
)

private fun rotationCoverMultiplier(rotation: Float): Float {
    val normalized = kotlin.math.abs(rotation) / 45f
    return 1f + normalized.coerceIn(0f, 1f) * 0.34f
}

private fun Long.toComposeColor(): Color = Color(this.toInt())
