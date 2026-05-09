package com.diary.moonpage.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diary.moonpage.presentation.components.core.layout.SectionTitle
import com.diary.moonpage.presentation.components.profile.ProfileHeader
import com.diary.moonpage.presentation.components.profile.ProfileMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
        ) {
            SectionTitle("Preferences")
            ProfileMenuItem(title = "Language", icon = Icons.Rounded.Language, onClick = {})
            ProfileMenuItem(title = "App Theme", icon = Icons.Rounded.Palette, onClick = {})
            
            SectionTitle("Security")
            ProfileMenuItem(title = "Passcode Lock", icon = Icons.Rounded.Lock, onClick = {})
            ProfileMenuItem(title = "Biometric Authentication", icon = Icons.Rounded.Fingerprint, onClick = {})

            SectionTitle("Support")
            ProfileMenuItem(title = "Help Center", icon = Icons.Rounded.Help, onClick = {})
            ProfileMenuItem(title = "Terms of Service", icon = Icons.Rounded.Description, onClick = {})
            ProfileMenuItem(title = "Privacy Policy", icon = Icons.Rounded.PrivacyTip, onClick = {})

            SectionTitle("Account Action")
            ProfileMenuItem(
                title = "Delete Account", 
                icon = Icons.Rounded.DeleteForever, 
                onClick = {},
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
