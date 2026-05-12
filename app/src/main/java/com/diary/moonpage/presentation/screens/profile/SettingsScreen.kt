package com.diary.moonpage.presentation.screens.profile

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diary.moonpage.presentation.components.core.layout.SectionTitle
import com.diary.moonpage.presentation.components.profile.ProfileMenuItem

import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCreatePasscode: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle(stringResource(R.string.preferences), modifier = Modifier.padding(bottom = 0.dp))
            ProfileMenuItem(
                title = stringResource(R.string.language), 
                icon = Icons.Rounded.Language, 
                trailingContent = {
                    Text(
                        text = when(uiState.language) {
                            "vi" -> "Tiếng Việt"
                            "fr" -> "Français"
                            else -> "English"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = { showLanguageDialog = true }
            )
            ProfileMenuItem(
                title = stringResource(R.string.app_theme), 
                icon = Icons.Rounded.Palette, 
                trailingContent = {
                    Text(
                        text = when(uiState.isDarkMode) {
                            true -> stringResource(R.string.dark)
                            false -> stringResource(R.string.light)
                            else -> stringResource(R.string.system)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = { showThemeDialog = true }
            )
            
            SectionTitle(stringResource(R.string.security), modifier = Modifier.padding(top = 12.dp, bottom = 0.dp))
            ProfileMenuItem(
                title = stringResource(R.string.passcode_lock), 
                icon = Icons.Rounded.Lock, 
                trailingContent = {
                    Switch(
                        checked = uiState.isPasscodeEnabled,
                        onCheckedChange = { 
                            if (it) onNavigateToCreatePasscode()
                            else viewModel.togglePasscode(false)
                        }
                    )
                },
                onClick = { 
                    if (!uiState.isPasscodeEnabled) onNavigateToCreatePasscode()
                    else viewModel.togglePasscode(false)
                }
            )
            ProfileMenuItem(
                title = stringResource(R.string.biometric_auth), 
                icon = Icons.Rounded.Fingerprint, 
                trailingContent = {
                    Switch(
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) }
                    )
                },
                onClick = { viewModel.toggleBiometric(!uiState.isBiometricEnabled) }
            )

            SectionTitle(stringResource(R.string.support), modifier = Modifier.padding(top = 12.dp, bottom = 0.dp))
            ProfileMenuItem(
                title = stringResource(R.string.help_center), 
                icon = Icons.Rounded.Help, 
                onClick = { uriHandler.openUri("https://moodify.com/help") }
            )
            ProfileMenuItem(
                title = stringResource(R.string.terms_of_service), 
                icon = Icons.Rounded.Description, 
                onClick = { uriHandler.openUri("https://moodify.com/terms") }
            )
            ProfileMenuItem(
                title = stringResource(R.string.privacy_policy), 
                icon = Icons.Rounded.PrivacyTip, 
                onClick = { uriHandler.openUri("https://moodify.com/privacy") }
            )

            SectionTitle(stringResource(R.string.account_action), modifier = Modifier.padding(top = 12.dp, bottom = 0.dp))
            ProfileMenuItem(
                title = stringResource(R.string.delete_account), 
                icon = Icons.Rounded.DeleteForever, 
                onClick = { viewModel.showDeleteAccountDialog() },
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.select_language)) },
            text = {
                Column {
                    val languages = listOf(
                        "en" to "English",
                        "vi" to "Tiếng Việt",
                        "fr" to "Français"
                    )
                    languages.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label)
                            RadioButton(
                                selected = uiState.language == code,
                                onClick = { 
                                    viewModel.setLanguage(code) {
                                        (context as? Activity)?.recreate()
                                    }
                                    showLanguageDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.app_theme)) },
            text = {
                Column {
                    val themes = listOf(
                        null to stringResource(R.string.system),
                        false to stringResource(R.string.light),
                        true to stringResource(R.string.dark)
                    )
                    themes.forEach { (isDark, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label)
                            RadioButton(
                                selected = uiState.isDarkMode == isDark,
                                onClick = { 
                                    viewModel.setTheme(isDark)
                                    showThemeDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (uiState.isDeleteAccountDialogShown) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteAccountDialog() },
            title = { Text(stringResource(R.string.delete_account)) },
            text = { Text(stringResource(R.string.delete_account_confirmation)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteUserAccount(onNavigateToLogin) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.delete))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteAccountDialog() }) {
                    Text(stringResource(R.string.cancel))
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
