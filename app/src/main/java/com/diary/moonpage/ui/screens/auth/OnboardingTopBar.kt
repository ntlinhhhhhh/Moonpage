package com.diary.moonpage.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diary.moonpage.R

/**
 * Shared top bar for all 5 onboarding steps.
 * Shows Back button + step dots indicator matching the image design.
 *
 * @param currentStep 1-indexed step (1..5)
 * @param totalSteps total number of steps (5)
 * @param onNavigateBack back button callback
 */
@Composable
fun OnboardingTopBar(
    currentStep: Int,
    totalSteps: Int = 5,
    onNavigateBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.Rounded.ArrowBackIosNew,
                contentDescription = stringResource(R.string.back),
                tint = colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Step dots indicator — matches the image: filled pill for current+done, dashes for future
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (step in 1..totalSteps) {
                val isDone = step <= currentStep
                if (isDone) {
                    // Filled segment — active/completed
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colorScheme.primary)
                    )
                } else {
                    // Dashed/dotted segment — future step
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colorScheme.onBackground.copy(alpha = 0.15f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(48.dp))
    }
}
