package com.diary.moonpage.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.Transgender
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R

private data class GenderOption(
    val value: String,
    val labelRes: Int,
    val icon: ImageVector
)

private val GENDER_OPTIONS = listOf(
    GenderOption("Female", R.string.gender_female, Icons.Rounded.Female),
    GenderOption("Male", R.string.gender_male, Icons.Rounded.Male),
    GenderOption("Other", R.string.gender_other, Icons.Rounded.Transgender)
)

/**
 * Stateful Component
 */
@Composable
fun OnboardingGenderRoute(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit
) {
    var selectedGender by remember { mutableStateOf("") }

    OnboardingGenderScreen(
        selectedGender = selectedGender,
        onGenderSelect = { selectedGender = it },
        onNavigateBack = onNavigateBack,
        onFinish = {
            viewModel.setGender(selectedGender)
            onFinish()
        }
    )
}

/**
 * Stateless Component
 */
@Composable
fun OnboardingGenderScreen(
    selectedGender: String,
    onGenderSelect: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Step 2 of 5 → 0.4f
    val progressAnim by animateFloatAsState(
        targetValue = 0.4f,
        animationSpec = tween(600),
        label = "progress"
    )

    Scaffold(containerColor = colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OnboardingTopBar(
                currentStep = 2,
                onNavigateBack = onNavigateBack
            )

            // ── Content ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.what_gender),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.gender_personalization),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Gender options
                GENDER_OPTIONS.forEach { option ->
                    val isSelected = selectedGender == option.value
                    GenderCard(
                        option = option,
                        isSelected = isSelected,
                        onClick = { onGenderSelect(option.value) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Next button (Step 2 → Step 3)
                Button(
                    onClick = onFinish,
                    enabled = selectedGender.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                        disabledContainerColor = colorScheme.primary.copy(alpha = 0.2f),
                        disabledContentColor = colorScheme.onPrimary.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.next),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GenderCard(
    option: GenderOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colorScheme.primary else colorScheme.onBackground.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        color = if (isSelected) colorScheme.primary.copy(alpha = 0.05f) else colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (isSelected) colorScheme.primary else colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(option.labelRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colorScheme.primary else colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = colorScheme.primary)
                )
            }
        }
    }
}
