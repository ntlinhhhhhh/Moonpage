package com.diary.moonpage.ui.screens.profile

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import com.diary.moonpage.core.util.LocaleUtils
import com.diary.moonpage.ui.screens.profile.components.*
import com.diary.moonpage.ui.components.layout.SectionTitle
import com.diary.moonpage.core.theme.*

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCreatePasscode: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showLanguageDialog by remember { mutableStateOf(false) }

    SettingsScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onLanguageClick = { showLanguageDialog = true },
        onThemeToggle = { viewModel.setTheme(it) },
        onPasscodeToggle = { enabled -> 
            if (enabled) onNavigateToCreatePasscode() 
            else viewModel.togglePasscode(false)
        },
        onBiometricToggle = { viewModel.toggleBiometric(it) },
        onReminderToggle = { viewModel.toggleReminder(it) },
        onReminderTimeClick = { hour, minute -> viewModel.updateReminderTime(hour, minute) },
        onDeleteAccountClick = { viewModel.showDeleteAccountDialog() }
    )

    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = uiState.language,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { lang ->
                viewModel.setLanguage(lang) {
                    // AppCompatDelegate.setApplicationLocales handles the change,
                    // but we can add additional logic here if needed.
                }
                showLanguageDialog = false
            }
        )
    }

    if (uiState.isDeleteAccountDialogShown) {
        DeleteAccountConfirmationDialog(
            onDismiss = { viewModel.dismissDeleteAccountDialog() },
            onConfirm = {
                viewModel.deleteUserAccount {
                    onNavigateToLogin()
                }
            }
        )
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // In a real app, show a snackbar or toast
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onLanguageClick: () -> Unit,
    onThemeToggle: (Boolean?) -> Unit,
    onPasscodeToggle: (Boolean) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeClick: (Int, Int) -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var showTimePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            SectionTitle(stringResource(R.string.preferences))

            SettingsMenuItem(
                title = stringResource(R.string.language),
                value = if (uiState.language == "vi") "Tiếng Việt" else "English",
                icon = Icons.Rounded.Language,
                onClick = onLanguageClick
            )

            ThemeSettingItem(
                currentSelection = uiState.isDarkMode,
                onSelectionChange = onThemeToggle
            )

            SectionTitle(stringResource(R.string.notifications))

            SwitchSettingItem(
                title = stringResource(R.string.daily_reminder),
                icon = Icons.Rounded.Notifications,
                checked = uiState.isReminderEnabled,
                onCheckedChange = onReminderToggle
            )

            if (uiState.isReminderEnabled) {
                SettingsMenuItem(
                    title = stringResource(R.string.reminder_time),
                    value = uiState.reminderTime,
                    icon = Icons.Rounded.Schedule,
                    onClick = { showTimePicker = true }
                )
            }

            SectionTitle(stringResource(R.string.security))

            SwitchSettingItem(
                title = stringResource(R.string.passcode_lock),
                icon = Icons.Rounded.Lock,
                checked = uiState.isPasscodeEnabled,
                onCheckedChange = onPasscodeToggle
            )

            SwitchSettingItem(
                title = stringResource(R.string.biometric_auth),
                icon = Icons.Rounded.Fingerprint,
                checked = uiState.isBiometricEnabled,
                onCheckedChange = onBiometricToggle,
                enabled = uiState.isPasscodeEnabled
            )

            SectionTitle(stringResource(R.string.support))

            SettingsMenuItem(title = stringResource(R.string.help_center), icon = Icons.Rounded.HelpOutline)
            SettingsMenuItem(title = stringResource(R.string.terms_of_service), icon = Icons.Rounded.Description)
            SettingsMenuItem(title = stringResource(R.string.privacy_policy), icon = Icons.Rounded.PrivacyTip)

            SectionTitle(stringResource(R.string.account_action))

            SettingsMenuItem(
                title = stringResource(R.string.delete_account),
                icon = Icons.Rounded.DeleteForever,
                titleColor = colorScheme.error,
                onClick = onDeleteAccountClick
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showTimePicker) {
        val initialTime = uiState.reminderTime.split(":")
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.getOrNull(0)?.toInt() ?: 21,
            initialMinute = initialTime.getOrNull(1)?.toInt() ?: 0
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onReminderTimeClick(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}
