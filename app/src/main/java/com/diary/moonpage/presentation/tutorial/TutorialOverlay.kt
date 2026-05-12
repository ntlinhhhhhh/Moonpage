package com.diary.moonpage.presentation.tutorial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TutorialOverlay(
    step: TutorialStep,
    onSkipStep: () -> Unit,
    onSkipTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBottomNavStep = step in setOf(TutorialStep.Stats, TutorialStep.Camera, TutorialStep.Store, TutorialStep.Profile)
    val message = when (step) {
        TutorialStep.PickToday -> "Start with today. Tap the current day to make your first record."
        TutorialStep.DailyActivities -> "You can record hobbies or activities from your day."
        TutorialStep.DailySleep -> "You can enter sleep time when you want to track rest."
        TutorialStep.DailyNote -> "You can write a note about anything you want to remember."
        TutorialStep.DailyPhoto -> "You can add a picture to keep the day more vivid."
        TutorialStep.Stats -> "After you have recorded some days, you can see your statistics here."
        TutorialStep.Camera -> "You can take a picture any time here."
        TutorialStep.Store -> "Get new themes here."
        TutorialStep.Profile -> "Customize yourself here."
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color.Black.copy(alpha = 0.48f))

            if (isBottomNavStep) {
                val itemWidth = size.width / 5f
                val index = when (step) {
                    TutorialStep.Stats -> 1
                    TutorialStep.Camera -> 2
                    TutorialStep.Store -> 3
                    TutorialStep.Profile -> 4
                    else -> 0
                }
                val center = Offset(itemWidth * index + itemWidth / 2f, size.height - 38.dp.toPx())
                drawCircle(
                    color = Color.White,
                    radius = 38.dp.toPx(),
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            } else {
                val rect = when (step) {
                    TutorialStep.PickToday -> Rect(
                        left = size.width * 0.40f,
                        top = size.height * 0.28f,
                        right = size.width * 0.58f,
                        bottom = size.height * 0.37f
                    )
                    TutorialStep.DailyActivities -> Rect(24.dp.toPx(), size.height * 0.34f, size.width - 24.dp.toPx(), size.height * 0.52f)
                    TutorialStep.DailySleep -> Rect(24.dp.toPx(), size.height * 0.42f, size.width - 24.dp.toPx(), size.height * 0.60f)
                    TutorialStep.DailyNote -> Rect(24.dp.toPx(), size.height * 0.48f, size.width - 24.dp.toPx(), size.height * 0.66f)
                    TutorialStep.DailyPhoto -> Rect(24.dp.toPx(), size.height * 0.52f, size.width - 24.dp.toPx(), size.height * 0.78f)
                    else -> Rect.Zero
                }
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.cornerPathEffect(14.dp.toPx())
                    )
                )
            }
        }

        Text(
            text = "Skip the tutorial",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(18.dp))
                .clickable { onSkipTutorial() }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            color = Color(0xFF2B2B2B),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Surface(
            modifier = Modifier
                .align(if (isBottomNavStep) Alignment.BottomCenter else Alignment.BottomStart)
                .padding(16.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onSkipStep) {
                        Text("Skip this step")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = onSkipStep,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }
}
