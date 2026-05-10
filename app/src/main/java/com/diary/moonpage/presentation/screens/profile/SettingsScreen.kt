package com.diary.moonpage.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val language by viewModel.language.collectAsState()
    val isPasscodeEnabled by viewModel.isPasscodeEnabled.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }

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
                        text = when(language) {
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
            ProfileMenuItem(title = stringResource(R.string.app_theme), icon = Icons.Rounded.Palette, onClick = {})
            
            SectionTitle(stringResource(R.string.security), modifier = Modifier.padding(top = 12.dp, bottom = 0.dp))
            ProfileMenuItem(
                title = stringResource(R.string.passcode_lock), 
                icon = Icons.Rounded.Lock, 
                trailingContent = {
                    Switch(
                        checked = isPasscodeEnabled,
                        onCheckedChange = { viewModel.togglePasscode(it) }
                    )
                },
                onClick = { viewModel.togglePasscode(!isPasscodeEnabled) }
            )
            ProfileMenuItem(
                title = stringResource(R.string.biometric_auth), 
                icon = Icons.Rounded.Fingerprint, 
                trailingContent = {
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) }
                    )
                },
                onClick = { viewModel.toggleBiometric(!isBiometricEnabled) }
            )

            SectionTitle(stringResource(R.string.support), modifier = Modifier.padding(top = 12.dp, bottom = 0.dp))
            ProfileMenuItem(title = stringResource(R.string.help_center), icon = Icons.Rounded.Help, onClick = {})
            ProfileMenuItem(title = stringResource(R.string.terms_of_service), icon = Icons.Rounded.Description, onClick = {})
            ProfileMenuItem(title = stringResource(R.string.privacy_policy), icon = Icons.Rounded.PrivacyTip, onClick = {})

            SectionTitle(stringResource(R.string.account_action), modifier = Modifier.padding(top = 12.dp, bottom = 0.dp))
            ProfileMenuItem(
                title = stringResource(R.string.delete_account), 
                icon = Icons.Rounded.DeleteForever, 
                onClick = {},
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
                                selected = language == code,
                                onClick = { 
                                    viewModel.setLanguage(code)
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
}
