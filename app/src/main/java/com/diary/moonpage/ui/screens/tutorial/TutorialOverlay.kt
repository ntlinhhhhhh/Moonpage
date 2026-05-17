package com.diary.moonpage.ui.screens.tutorial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TutorialOverlay(
    step: TutorialStep,
    targetBounds: Rect?,
    onSkipStep: () -> Unit,
    onSkipTutorial: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = when (step) {
        TutorialStep.HighlightCurrentDay -> "Start logging your day today!"
        TutorialStep.HighlightMoodSelection -> "How are you feeling today? Select a mood."
        TutorialStep.HighlightActivityCategory -> "Select an activity category to open the list."
        TutorialStep.HighlightActivitySelection -> "What did you do today? Select an activity."
        TutorialStep.HighlightSleepSelection -> "How long did you sleep? Select your sleep hours."
        TutorialStep.HighlightDoneButton -> "Once finished, tap here to save your log."
        TutorialStep.HighlightStreak -> "Wonderful! You've completed your first log entry."
        TutorialStep.HighlightStatsTab -> "View detailed statistics of your mood and habits."
        TutorialStep.HighlightYearlyReport -> "Explore your monthly mood trends. Tap here to view details."
        TutorialStep.HighlightMoodDetailChart -> "This is your detailed monthly mood analysis chart."
        TutorialStep.HighlightMoodDetailBackButton -> "Tap here to return to the statistics page."
        TutorialStep.HighlightMomentTab -> "Review all the photos you have captured."
        TutorialStep.HighlightCameraCapture -> "Capture a new moment or upload your image."
        TutorialStep.HighlightMomentHistoryButton -> if (targetBounds == null) "This is your moment history feed. Swipe or tap to view your captured photos." else "Tap here to view your captured photos and history feed."
        TutorialStep.HighlightStoreTab -> "Explore beautiful new themes and icons here."
        TutorialStep.HighlightStoreThemes -> "Discover gorgeous diary themes. Tap a theme to view details."
        TutorialStep.HighlightThemeDetailApply -> "Preview and apply this theme to your diary."
        TutorialStep.HighlightThemeDetailBackButton -> "Tap here to return to the store."
        TutorialStep.HighlightProfileTab -> "Manage your account and settings."
        TutorialStep.HighlightProfileSettings -> "Manage your account info. Tap here to open account settings."
        TutorialStep.HighlightAccountInfo -> "Here is where you can change your avatar and username."
        TutorialStep.HighlightAccountBackButton -> "Tap here to complete the tutorial."
    }

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 4 Blocker Boxes to intercept clicks outside targetBounds, allowing touch-through inside the hole
        if (targetBounds != null) {
            val isBackButtonStep = step == TutorialStep.HighlightMoodDetailBackButton ||
                                   step == TutorialStep.HighlightThemeDetailBackButton ||
                                   step == TutorialStep.HighlightAccountBackButton
            
            val paddingPx = with(density) {
                if (isBackButtonStep) (-6).dp.toPx() else 8.dp.toPx()
            }
            val rect = targetBounds.inflate(paddingPx)
            
            val rectTopDp = with(density) { rect.top.toDp() }
            val rectBottomDp = with(density) { rect.bottom.toDp() }
            val rectLeftDp = with(density) { rect.left.toDp() }
            val rectRightDp = with(density) { rect.right.toDp() }
            val rectHeightDp = with(density) { rect.height.toDp() }

            val screenWidthDp = config.screenWidthDp.dp
            val screenHeightDp = config.screenHeightDp.dp

            // Top Blocker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rectTopDp.coerceAtLeast(0.dp))
                    .align(Alignment.TopStart)
                    .blockTouches()
            )

            // Bottom Blocker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeightDp - rectBottomDp).coerceAtLeast(0.dp))
                    .offset(y = rectBottomDp)
                    .align(Alignment.TopStart)
                    .blockTouches()
            )

            // Left Blocker
            Box(
                modifier = Modifier
                    .width(rectLeftDp.coerceAtLeast(0.dp))
                    .height(rectHeightDp.coerceAtLeast(0.dp))
                    .offset(y = rectTopDp)
                    .align(Alignment.TopStart)
                    .blockTouches()
            )

            // Right Blocker
            Box(
                modifier = Modifier
                    .width((screenWidthDp - rectRightDp).coerceAtLeast(0.dp))
                    .height(rectHeightDp.coerceAtLeast(0.dp))
                    .offset(x = rectRightDp, y = rectTopDp)
                    .align(Alignment.TopStart)
                    .blockTouches()
            )
        } else {
            // Full screen blocker if no target bounds
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blockTouches()
            )
        }

        // Spotlight Visual Layer (Zero touch handling, just drawing)
         Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Force an offscreen layer so BlendMode.Clear actually punches a hole
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            // 1. Draw the semi-transparent black overlay over the whole screen
            drawRect(Color.Black.copy(alpha = 0.7f))

            // 2. Punch a hole where the target is
            if (targetBounds != null) {
                val isBackButtonStep = step == TutorialStep.HighlightMoodDetailBackButton ||
                                       step == TutorialStep.HighlightThemeDetailBackButton ||
                                       step == TutorialStep.HighlightAccountBackButton
                
                val padding = if (isBackButtonStep) (-6).dp.toPx() else 8.dp.toPx()
                val rect = targetBounds.inflate(padding)
                
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    cornerRadius = CornerRadius((if (isBackButtonStep) 12.dp else 16.dp).toPx()),
                    blendMode = BlendMode.Clear // This punches the hole!
                )
            }
        }

        // Skip Tutorial Button at top right
        Text(
            text = "Skip",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 20.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .clickable { onSkipTutorial() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        val showNextButton = when (step) {
            TutorialStep.HighlightCurrentDay -> false
            TutorialStep.HighlightMoodSelection -> true
            TutorialStep.HighlightActivityCategory -> true
            TutorialStep.HighlightActivitySelection -> true
            TutorialStep.HighlightSleepSelection -> true
            TutorialStep.HighlightDoneButton -> false
            TutorialStep.HighlightStreak -> true
            
            TutorialStep.HighlightStatsTab -> false
            TutorialStep.HighlightYearlyReport -> false // Must click overview card
            TutorialStep.HighlightMoodDetailChart -> true // Click Next
            TutorialStep.HighlightMoodDetailBackButton -> false // Must click back
            
            TutorialStep.HighlightMomentTab -> false
            TutorialStep.HighlightCameraCapture -> true // Click Next
            TutorialStep.HighlightMomentHistoryButton -> true // Click Next
            
            TutorialStep.HighlightStoreTab -> false
            TutorialStep.HighlightStoreThemes -> false // Must click theme card
            TutorialStep.HighlightThemeDetailApply -> true // Click Next
            TutorialStep.HighlightThemeDetailBackButton -> false // Must click back
            
            TutorialStep.HighlightProfileTab -> false
            TutorialStep.HighlightProfileSettings -> false // Must click user info card
            TutorialStep.HighlightAccountInfo -> true // Click Next
            TutorialStep.HighlightAccountBackButton -> false // Must click back to complete
        }

        // Tooltip Box dynamically positioned
        if (targetBounds != null) {
            val tooltipPadding = 24.dp
            val yCenter = targetBounds.center.y
            // If the target is in the bottom half of the screen, show tooltip ABOVE it.
            // If it's in the top half, show tooltip BELOW it.
            // Also, only show above if there is enough space at the top of the screen (top > 240dp)
            val showAbove = yCenter > (screenHeightPx / 2) && targetBounds.top > with(density) { 240.dp.toPx() }

            val yOffset = with(density) {
                if (showAbove) {
                    // Tooltip bottom edge should be slightly above the target's top edge
                    (targetBounds.top - 20.dp.toPx()).toDp()
                } else {
                    // Tooltip top edge should be slightly below the target's bottom edge.
                    // Enforce a minimum safe offset of 116.dp so it is never cut off by the status bar!
                    val calculatedOffset = targetBounds.bottom + 20.dp.toPx()
                    val minSafeOffset = 116.dp.toPx()
                    calculatedOffset.coerceAtLeast(minSafeOffset).toDp()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tooltipPadding)
                    // Offset vertically based on calculation, and shift origin if showing above
                    .offset(y = if (showAbove) 0.dp else yOffset)
                    .align(if (showAbove) Alignment.TopCenter else Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                // If showing above, we need to push it down so its BOTTOM is at yOffset
                // We can use a Column with a Spacer pushing it down to yOffset
                if (showAbove) {
                    Column(modifier = Modifier.fillMaxWidth().height(yOffset), verticalArrangement = Arrangement.Bottom) {
                        TooltipContent(message = message, showNextButton = showNextButton, onNextClick = onNextClick)
                    }
                } else {
                    TooltipContent(message = message, showNextButton = showNextButton, onNextClick = onNextClick)
                }
            }
        } else {
            val tooltipPadding = 24.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 116.dp)
                    .padding(horizontal = tooltipPadding)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                TooltipContent(message = message, showNextButton = showNextButton, onNextClick = onNextClick)
            }
        }
    }
}

@Composable
private fun TooltipContent(
    message: String,
    showNextButton: Boolean,
    onNextClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF4CAF50), // Green background as requested
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            if (showNextButton) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Continue",
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .clickable { onNextClick() }
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun Modifier.blockTouches(): Modifier {
    return this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = {}
    )
}