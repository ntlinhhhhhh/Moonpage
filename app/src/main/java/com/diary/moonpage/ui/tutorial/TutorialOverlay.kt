package com.diary.moonpage.ui.tutorial

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
    targetBounds: Rect?,
    onSkipStep: () -> Unit,
    onSkipTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSkipStep = step in setOf(
        TutorialStep.HighlightHobbiesButton,
        TutorialStep.HighlightFirstHobby,
        TutorialStep.HighlightTodayNotes,
        TutorialStep.HighlightTodayPhotos
    )
    val message = when (step) {
        TutorialStep.HighlightCurrentDay -> "Tap today to start recording your day."
        TutorialStep.HighlightHowWasYourDay -> "How was your day? Choose your mood first."
        TutorialStep.HighlightHobbiesButton -> "Open hobbies to choose what you did today."
        TutorialStep.HighlightFirstHobby -> "Choose a hobby you did today."
        TutorialStep.HighlightTodayNotes -> "Write a short note for today."
        TutorialStep.HighlightTodayPhotos -> "Add one or more photos for today."
        TutorialStep.HighlightDoneButton -> "Tap Done to save today's record."
    }
    val showMessageAtTop = step == TutorialStep.HighlightTodayPhotos ||
        step == TutorialStep.HighlightDoneButton

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color.Black.copy(alpha = 0.48f))

            val fallbackRect = when (step) {
                TutorialStep.HighlightCurrentDay -> Rect(
                    left = size.width * 0.40f,
                    top = size.height * 0.28f,
                    right = size.width * 0.58f,
                    bottom = size.height * 0.37f
                )
                TutorialStep.HighlightHowWasYourDay -> Rect(24.dp.toPx(), size.height * 0.20f, size.width - 24.dp.toPx(), size.height * 0.38f)
                TutorialStep.HighlightHobbiesButton -> Rect(24.dp.toPx(), size.height * 0.32f, size.width - 24.dp.toPx(), size.height * 0.42f)
                TutorialStep.HighlightFirstHobby -> Rect(24.dp.toPx(), size.height * 0.38f, size.width * 0.45f, size.height * 0.48f)
                TutorialStep.HighlightTodayNotes -> Rect(24.dp.toPx(), size.height * 0.48f, size.width - 24.dp.toPx(), size.height * 0.66f)
                TutorialStep.HighlightTodayPhotos -> Rect(24.dp.toPx(), size.height * 0.58f, size.width - 24.dp.toPx(), size.height * 0.80f)
                TutorialStep.HighlightDoneButton -> Rect(16.dp.toPx(), size.height - 110.dp.toPx(), size.width - 16.dp.toPx(), size.height - 20.dp.toPx())
            }
            val rect = (targetBounds ?: fallbackRect).inflate(6.dp.toPx())
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

        Text(
            text = "Skip all steps",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                .clickable { onSkipTutorial() }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Surface(
            modifier = Modifier
                .align(
                    if (showMessageAtTop) Alignment.TopStart else Alignment.BottomStart
                )
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (showMessageAtTop) 96.dp else 16.dp,
                    bottom = 16.dp
                )
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
                    if (canSkipStep) {
                        OutlinedButton(onClick = onSkipStep) { Text("Skip this step") }
                    }
                }
            }
        }
    }
}
