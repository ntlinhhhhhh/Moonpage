package com.diary.moonpage.ui.screens.profile

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import com.diary.moonpage.core.util.BatteryOptimizationHelper
import com.diary.moonpage.core.util.ExactAlarmHelper
import com.diary.moonpage.core.util.LocaleUtils
import com.diary.moonpage.ui.screens.profile.components.*
import com.diary.moonpage.ui.components.layout.SectionTitle
import com.diary.moonpage.ui.components.inputs.MoonTimePicker
import com.diary.moonpage.core.theme.*
import com.diary.moonpage.ui.components.feedback.GlobalSnackbarManager
import com.diary.moonpage.ui.components.feedback.SnackbarType
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCreatePasscode: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }
    var showExactAlarmDialog by remember { mutableStateOf(false) }
    val passwordChangedMessage = stringResource(R.string.password_changed_successfully)

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
        onBatteryOptimizationClick = { showBatteryDialog = true },
        onExactAlarmClick = { showExactAlarmDialog = true },
        onChangePasswordClick = { showChangePasswordDialog = true },
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

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { old, new ->
                viewModel.changePassword(old, new) {
                    showChangePasswordDialog = false
                    scope.launch {
                        GlobalSnackbarManager.show(passwordChangedMessage, SnackbarType.SUCCESS)
                    }
                }
            }
        )
    }

    if (showBatteryDialog) {
        BatteryOptimizationDialog(
            onDismiss = { showBatteryDialog = false },
            onOpenSettings = {
                showBatteryDialog = false
                BatteryOptimizationHelper.openBatteryOptimizationSettings(context)
            }
        )
    }

    if (showExactAlarmDialog) {
        ExactAlarmDialog(
            onDismiss = { showExactAlarmDialog = false },
            onOpenSettings = {
                showExactAlarmDialog = false
                ExactAlarmHelper.openExactAlarmSettings(context)
            }
        )
    }

    if (uiState.isDeleteAccountDialogShown) {
        DeleteAccountConfirmationDialog(
            onDismiss = { viewModel.dismissDeleteAccountDialog() },
            onConfirm = {
                if (!uiState.authProvider.equals("Google", ignoreCase = true)) {
                    viewModel.showPasswordConfirmationDialog()
                } else if (uiState.authProvider.equals("Google", ignoreCase = true)) {
                    viewModel.dismissDeleteAccountDialog()
                    val googleWebClientId = context.getString(R.string.default_web_client_id)
                    scope.launch {
                        try {
                            val credentialManager = androidx.credentials.CredentialManager.create(context)
                            val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(googleWebClientId)
                                .setAutoSelectEnabled(false)
                                .build()
                            val request = androidx.credentials.GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential
                            if (credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdToken = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                                viewModel.confirmGoogleAndDelete(googleIdToken.idToken) {
                                    onNavigateToLogin()
                                }
                            }
                        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                            // User cancelled
                        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                            viewModel.setErrorRes(R.string.google_confirmation_required)
                        } catch (e: Exception) {
                            viewModel.setError(e.message ?: "")
                            if (e.message == null) {
                                viewModel.setErrorRes(R.string.google_confirmation_failed)
                            }
                        }
                    }
                }
            }
        )
    }

    if (uiState.isPasswordConfirmationDialogShown) {
        ConfirmPasswordDialog(
            onDismiss = { viewModel.dismissPasswordConfirmationDialog() },
            onConfirm = { password ->
                viewModel.confirmPasswordAndDelete(password) {
                    onNavigateToLogin()
                }
            }
        )
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            GlobalSnackbarManager.show(error, SnackbarType.ERROR)
            viewModel.clearError()
        }
    }

    uiState.errorResId?.let { errorResId ->
        val error = stringResource(errorResId)
        LaunchedEffect(errorResId) {
            GlobalSnackbarManager.show(error, SnackbarType.ERROR)
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
    onBatteryOptimizationClick: () -> Unit,
    onExactAlarmClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = stringResource(R.string.back),
                        tint = colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.settings),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorScheme.onBackground
                )
            }
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
                value = if (uiState.language == "en") {
                    stringResource(R.string.language_english)
                } else {
                    stringResource(R.string.language_vietnamese)
                },
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

            if (!ExactAlarmHelper.canScheduleExactAlarms(context)) {
                SettingsMenuItem(
                    title = stringResource(R.string.exact_alarm_title),
                    value = stringResource(R.string.recommended),
                    icon = Icons.Rounded.AlarmOn,
                    onClick = onExactAlarmClick
                )
            }

            if (!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)) {
                SettingsMenuItem(
                    title = stringResource(R.string.battery_unrestricted_title),
                    value = stringResource(R.string.recommended),
                    icon = Icons.Rounded.BatteryAlert,
                    onClick = onBatteryOptimizationClick
                )
            }

            SectionTitle(stringResource(R.string.security))

            if (!uiState.authProvider.equals("Google", ignoreCase = true)) {
                SettingsMenuItem(
                    title = stringResource(R.string.change_password),
                    icon = Icons.Rounded.LockOpen,
                    onClick = onChangePasswordClick
                )
            }

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
            containerColor = colorScheme.surface,
            tonalElevation = 0.dp,
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
                MoonTimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
private fun BatteryOptimizationDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.BatterySaver,
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(R.string.battery_unrestricted_title))
        },
        text = {
            Text(text = stringResource(R.string.battery_unrestricted_desc))
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.not_now))
            }
        }
    )
}

@Composable
private fun ExactAlarmDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.AlarmOn,
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(R.string.exact_alarm_title))
        },
        text = {
            Text(text = stringResource(R.string.exact_alarm_desc))
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.not_now))
            }
        }
    )
}
