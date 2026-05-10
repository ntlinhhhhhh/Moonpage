package com.diary.moonpage.presentation.screens.store

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.diary.moonpage.presentation.components.core.buttons.MoonPrimaryButton
import com.diary.moonpage.presentation.components.core.feedback.MoonSnackbarHost
import com.diary.moonpage.presentation.screens.store.components.*
import com.diary.moonpage.core.theme.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDetailScreen(
    viewModel: StoreViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = uiState.selectedThemeDetail
    val snackbarHostState = remember { SnackbarHostState() }

    if (theme == null) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            if (effect is StoreUiEffect.ShowSnackBar) {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onBackground = MaterialTheme.colorScheme.onBackground

    val shakeOffset = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current
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
                    .height(64.dp)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = onBackground
                    )
                }

                Text(
                    text = "Theme Detail",
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
                        .graphicsLayer(translationX = shakeOffset.value)
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
                            Text(
                                text = "$",
                                modifier = Modifier.offset(y = (-0.8).dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                    text = theme.description ?: "Experience the beauty of this unique set.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                ThemeCalendarPreview(theme = theme)

                Spacer(modifier = Modifier.height(32.dp))

                MoonPrimaryButton(
                    text = if (theme.isOwned) "Activate" else "Buy for ${theme.price} $",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (theme.isOwned) {
                            viewModel.activateTheme(theme.id)
                        } else {
                            viewModel.initiatePurchase(theme)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "INCLUDES: CALENDAR ICONS, PREMIUM BACKGROUND, CUSTOM UI TONES",
                    style = MaterialTheme.typography.labelSmall,
                    color = onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Snackbar at top
            MoonSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.TopCenter),
                topPadding = 16.dp
            )

            if (uiState.showConfirmPurchaseDialog && uiState.themeToPurchase != null) {
                ConfirmPurchaseDialog(
                    theme = uiState.themeToPurchase!!,
                    onConfirm = { viewModel.buyTheme(uiState.themeToPurchase!!) },
                    onCancel = { viewModel.cancelPurchase() }
                )
            }

            if (uiState.showConfirmActivationDialog && uiState.selectedThemeDetail != null) {
                ConfirmActivationDialog(
                    themeName = uiState.selectedThemeDetail?.name ?: "",
                    onConfirm = { viewModel.confirmActivation() },
                    onCancel = { viewModel.cancelActivation() }
                )
            }

            if (uiState.showPurchaseSuccessDialog && uiState.purchasedTheme != null) {
                PurchaseSuccessDialog(
                    themeName = uiState.purchasedTheme?.name ?: "",
                    onDismiss = { viewModel.dismissDialog() }
                )
            }
        }
    }
}

@Composable
fun ThemeCalendarPreview(theme: Theme) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
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
        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
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
