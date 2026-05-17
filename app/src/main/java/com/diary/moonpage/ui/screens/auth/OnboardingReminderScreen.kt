package com.diary.moonpage.ui.screens.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.diary.moonpage.core.util.ReminderManager
import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Icon
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Alarm,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Text(
            text = stringResource(R.string.daily_reflection_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(R.string.reminder_question),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Time Display/Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            onClick = onTimeClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 4.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Finish Button
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = stringResource(R.string.get_started),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (showTimePicker) {
            DatePickerDialog(
                onDismissRequest = onTimePickerDismiss,
                confirmButton = {
                    TextButton(onClick = onTimePickerConfirm) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onTimePickerDismiss) {
                        Text("Cancel")
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        }
    }
}
