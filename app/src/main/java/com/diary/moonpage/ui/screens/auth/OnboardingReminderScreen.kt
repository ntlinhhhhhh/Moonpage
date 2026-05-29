package com.diary.moonpage.ui.screens.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.diary.moonpage.core.util.ReminderManager
import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R
import com.diary.moonpage.ui.components.inputs.MoonTimePicker

/**
 * Stateful Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingReminderRoute(
    viewModel: OnboardingViewModel,
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    var selectedHour by remember { mutableIntStateOf(21) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setReminderEnabled(isGranted)
        viewModel.setReminderTime(String.format("%02d:%02d", selectedHour, selectedMinute))
        
        if (isGranted) {
            com.diary.moonpage.core.util.ReminderManager(context).scheduleDailyReminder(selectedHour, selectedMinute)
        }
        onFinish()
    }

    OnboardingReminderScreen(
        selectedHour = selectedHour,
        selectedMinute = selectedMinute,
        showTimePicker = showTimePicker,
        timePickerState = timePickerState,
        onTimeClick = { showTimePicker = true },
        onTimePickerDismiss = { showTimePicker = false },
        onTimePickerConfirm = {
            selectedHour = timePickerState.hour
            selectedMinute = timePickerState.minute
            showTimePicker = false
        },
        onNavigateBack = onNavigateBack,
        onFinish = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                
                if (isGranted) {
                    viewModel.setReminderEnabled(true)
                    viewModel.setReminderTime(String.format("%02d:%02d", selectedHour, selectedMinute))
                    com.diary.moonpage.core.util.ReminderManager(context).scheduleDailyReminder(selectedHour, selectedMinute)
                    onFinish()
                } else {
                    permissionLauncher.launch(permission)
                }
            } else {
                viewModel.setReminderEnabled(true)
                viewModel.setReminderTime(String.format("%02d:%02d", selectedHour, selectedMinute))
                com.diary.moonpage.core.util.ReminderManager(context).scheduleDailyReminder(selectedHour, selectedMinute)
                onFinish()
            }
        }
    )
}

/**
 * Stateless Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingReminderScreen(
    selectedHour: Int,
    selectedMinute: Int,
    showTimePicker: Boolean,
    timePickerState: TimePickerState,
    onTimeClick: () -> Unit,
    onTimePickerDismiss: () -> Unit,
    onTimePickerConfirm: () -> Unit,
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Step 5 of 5 → 1.0f
    val progressAnim by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = tween(600),
        label = "progress"
    )

    Scaffold(containerColor = colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar ──────────────────────────────────────────
            OnboardingTopBar(
                currentStep = 5,
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Alarm icon
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(24.dp),
                color = colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Header
            Text(
                text = stringResource(R.string.daily_reflection_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.reminder_question),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = colorScheme.onBackground.copy(alpha = 0.6f),
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Time Display — LARGE & BOLD ───────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(24.dp),
                color = colorScheme.primary.copy(alpha = 0.08f),
                onClick = onTimeClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMinute),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        letterSpacing = 2.sp,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Done Button ───────────────────────────────────────────────────
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.done),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── Time Picker Dialog ────────────────────────────────────────────
            if (showTimePicker) {
                DatePickerDialog(
                    onDismissRequest = onTimePickerDismiss,
                    confirmButton = {
                        TextButton(onClick = onTimePickerConfirm) {
                            Text(stringResource(R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onTimePickerDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MoonTimePicker(state = timePickerState)
                    }
                }
            }
        }
    }
}
