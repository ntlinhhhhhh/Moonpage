package com.diary.moonpage.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diary.moonpage.R

/**
 * Stateful Component
 */
@Composable
fun MenstrualCycleRoute(
    onNavigateBack: () -> Unit
) {
    MenstrualCycleScreen(
        onNavigateBack = onNavigateBack
    )
}

/**
 * Stateless Component
 */
@Composable
fun MenstrualCycleScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = stringResource(R.string.back))
                }
                Text(
                    stringResource(R.string.menstrual_cycle_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.menstrual_cycle_placeholder), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
