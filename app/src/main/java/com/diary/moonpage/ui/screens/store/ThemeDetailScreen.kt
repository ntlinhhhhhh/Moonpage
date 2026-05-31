package com.diary.moonpage.ui.screens.store

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.ui.components.buttons.MoonPrimaryButton
import com.diary.moonpage.ui.components.feedback.GlobalSnackbarManager
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.ui.components.feedback.SnackbarType
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.ui.screens.store.components.ConfirmActivationDialog
import com.diary.moonpage.ui.screens.store.components.ConfirmPurchaseDialog
import com.diary.moonpage.ui.screens.store.components.CuteBeanIcon
import com.diary.moonpage.ui.screens.store.components.PurchaseSuccessDialog
import com.diary.moonpage.ui.screens.store.components.getThemeShades
import kotlinx.coroutines.delay
import com.diary.moonpage.ui.screens.tutorial.tutorialTarget
import com.diary.moonpage.ui.screens.tutorial.TutorialStep
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.diary.moonpage.core.util.normalizeAppImageUrl
import com.diary.moonpage.core.theme.previewBackgroundImagePath
import com.diary.moonpage.core.theme.previewBackgroundBrush

@Composable
fun ThemeDetailRoute(
    viewModel: StoreViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = uiState.selectedThemeDetail
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    if (theme == null) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is StoreUiEffect.ShowSnackBar -> {
                    GlobalSnackbarManager.show(effect.message, effect.type)
                }
                is StoreUiEffect.ThemeActivated -> {
                    GlobalSnackbarManager.show(
                        effect.message ?: UiText.StringResource(R.string.theme_updated_success),
                        SnackbarType.SUCCESS
                    )
                }
                is StoreUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(uiState.activationSuccess) {
        if (uiState.activationSuccess) {
            delay(1000)
            viewModel.dismissSuccessMessage()
        }
    }

    ThemeDetailScreen(
        uiState = uiState,
        theme = theme,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onActivateClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.activateTheme(theme.id)
        },
        onBuyClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.initiatePurchase(theme)
        },
        onConfirmPurchase = { viewModel.buyTheme(uiState.themeToPurchase!!) },
        onCancelPurchase = { viewModel.cancelPurchase() },
        onConfirmActivation = { viewModel.confirmActivation() },
        onCancelActivation = { viewModel.cancelActivation() },
        onDismissDialog = { viewModel.dismissDialog() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDetailScreen(
    uiState: StoreUiState,
    theme: Theme,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onActivateClick: () -> Unit,
    onBuyClick: () -> Unit,
    onConfirmPurchase: () -> Unit,
    onCancelPurchase: () -> Unit,
    onConfirmActivation: () -> Unit,
    onCancelActivation: () -> Unit,
    onDismissDialog: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onBackground = MaterialTheme.colorScheme.onBackground

    val shakeOffset = remember { Animatable(0f) }
    
    LaunchedEffect(uiState.showPurchaseSuccessDialog) {
        if (uiState.showPurchaseSuccessDialog) {
            repeat(6) {
                shakeOffset.animateTo(
                    targetValue = if (it % 2 == 0) 10f else -10f,
                    animationSpec = tween(durationMillis = 50)
                )
            }
            shakeOffset.animateTo(0f)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .tutorialTarget(TutorialStep.HighlightThemeDetailBackButton)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = stringResource(R.string.back),
                        tint = onBackground
                    )
                }

                Text(
                    text = stringResource(R.string.theme_detail),
                    style = MaterialTheme.typography.titleMedium,
                    color = onBackground
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .height(32.dp)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterEnd)
                        .graphicsLayer {
                            translationX = shakeOffset.value
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${uiState.userCoins}",
                            style = MaterialTheme.typography.labelLarge,
                            color = onSurface,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = onBackground,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = theme.description.themeDetailText()
                        ?: stringResource(R.string.theme_description_default),
                    style = MaterialTheme.typography.bodyLarge,
                    color = onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                ThemeCalendarPreview(theme = theme)

                Spacer(modifier = Modifier.height(32.dp))

                val buttonText = if (theme.isOwned) stringResource(R.string.activate) else stringResource(R.string.buy_for, theme.price)
                
                com.diary.moonpage.core.theme.MoonPageTheme(
                    themeType = theme.toMoonThemeType(),
                    activeTheme = theme
                ) {
                    MoonPrimaryButton(
                        text = buttonText,
                        onClick = {
                            if (theme.isOwned) {
                                onActivateClick()
                            } else {
                                onBuyClick()
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .tutorialTarget(TutorialStep.HighlightThemeDetailApply)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.theme_includes),
                    style = MaterialTheme.typography.labelSmall,
                    color = onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
            if (uiState.showConfirmPurchaseDialog && uiState.themeToPurchase != null) {
                com.diary.moonpage.core.theme.MoonPageTheme(
                    themeType = uiState.themeToPurchase!!.toMoonThemeType(),
                    activeTheme = uiState.themeToPurchase
                ) {
                    ConfirmPurchaseDialog(
                        theme = uiState.themeToPurchase!!,
                        onConfirm = onConfirmPurchase,
                        onCancel = onCancelPurchase
                    )
                }
            }

            if (uiState.showConfirmActivationDialog && uiState.selectedThemeDetail != null) {
                com.diary.moonpage.core.theme.MoonPageTheme(
                    themeType = uiState.selectedThemeDetail!!.toMoonThemeType(),
                    activeTheme = uiState.selectedThemeDetail
                ) {
                    ConfirmActivationDialog(
                        themeName = uiState.selectedThemeDetail?.name ?: "",
                        onConfirm = onConfirmActivation,
                        onCancel = onCancelActivation,
                        primaryColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (uiState.showPurchaseSuccessDialog && uiState.purchasedTheme != null) {
                PurchaseSuccessDialog(
                    themeName = uiState.purchasedTheme?.name ?: "",
                    onDismiss = onDismissDialog
                )
            }
        }
    }
}

private fun String?.themeDetailText(): String? {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) return null
    if ((value.startsWith("{") && value.endsWith("}")) || (value.startsWith("[") && value.endsWith("]"))) return null
    return value
}

@Composable
fun ThemeCalendarPreview(theme: Theme) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val isDark = isSystemInDarkTheme()
    val previewPath = remember(theme) { theme.previewBackgroundImagePath(isDark) }
    val previewBrush = remember(theme) { theme.previewBackgroundBrush(isDark) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
    ) {
        if (previewPath != null) {
            val normalizedModel = remember(previewPath) { normalizeAppImageUrl(previewPath) }
            AsyncImage(
                model = normalizedModel,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            // Visual protection scrim
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(if (isDark) Color.Black.copy(alpha = 0.58f) else Color.White.copy(alpha = 0.50f))
            )
        } else if (previewBrush != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(previewBrush)
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2025.04",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Days of week
            val days = listOf(
                stringResource(R.string.sun),
                stringResource(R.string.mon),
                stringResource(R.string.tue),
                stringResource(R.string.wed),
                stringResource(R.string.thu),
                stringResource(R.string.fri),
                stringResource(R.string.sat)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.width(44.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shaded Color Range Logic
            val actualShades = getThemeShades(theme)

            Column(modifier = Modifier.fillMaxWidth()) {
                repeat(4) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(7) { colIndex ->
                            val iconIndex = (rowIndex + colIndex) % 5

                            CuteBeanIcon(
                                modifier = Modifier.size(40.dp),
                                emotion = theme.icons.getOrElse(iconIndex) { "NEUTRAL" },
                                decoration = theme.decoration,
                                color = actualShades.getOrElse(iconIndex) { Color.LightGray }
                            )
                        }
                    }
                }
            }
        }
    }
}
