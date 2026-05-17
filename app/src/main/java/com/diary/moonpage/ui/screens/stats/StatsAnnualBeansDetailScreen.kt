package com.diary.moonpage.ui.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.ui.screens.stats.components.*

@Composable
fun StatsAnnualBeansDetailRoute(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val viewContext = LocalView.current
    
    val flatGridList = uiState.stats?.yearlyMoodGrid ?: uiState.stats?.moodFlow ?: emptyList()
    
    StatsAnnualBeansDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onShareRecap = {
            viewModel.clearCaptureError()
            com.diary.moonpage.core.util.ComposeCaptureUtils.captureComposable(
                view = viewContext,
                content = {
                    com.diary.moonpage.core.theme.MoonPageTheme {
                        YearlyRecapCard(
                            year = uiState.selectedYear,
                            totalLogs = uiState.stats?.totalLogs ?: 0,
                            totalPhotos = uiState.stats?.totalPhotos ?: 0,
                            yearlyMoodGrid = flatGridList,
                            themeType = uiState.themeType,
                            bestActivities = uiState.stats?.bestActivities ?: emptyList(),
                            averageDistance = uiState.stats?.averageDistance ?: 0.0,
                            averageSteps = uiState.stats?.averageSteps?.toInt() ?: 0,
                            longestStreak = uiState.stats?.longestStreak ?: 0
                        )
                    }
                },
                width = 1080,
                onBitmapCaptured = { bitmap -> viewModel.saveRecapToGallery(context, bitmap) },
                onFailure = {}
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsAnnualBeansDetailScreen(
    uiState: StatisticsUiState,
    onBack: () -> Unit,
    onShareRecap: () -> Unit
) {
    val stats = uiState.stats
    val flatGridList = stats?.yearlyMoodGrid ?: stats?.moodFlow ?: emptyList()
    
    var showRecapDetail by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (!showRecapDetail) {
                            stringResource(id = com.diary.moonpage.R.string.year_in_beans)
                        } else {
                            "Your Recap"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (showRecapDetail) {
                                showRecapDetail = false
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (showRecapDetail) {
                        IconButton(onClick = onShareRecap) {
                            Icon(androidx.compose.material.icons.Icons.Rounded.Download, contentDescription = "Download & Share")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (!showRecapDetail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                YearInMoonpageMiniatureCard(
                    year = uiState.selectedYear,
                    themeType = uiState.themeType,
                    onDetailClick = { showRecapDetail = true }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                YearlyRecapCard(
                    year = uiState.selectedYear,
                    totalLogs = stats?.totalLogs ?: 0,
                    totalPhotos = stats?.totalPhotos ?: 0,
                    yearlyMoodGrid = flatGridList,
                    themeType = uiState.themeType,
                    bestActivities = stats?.bestActivities ?: emptyList(),
                    averageDistance = stats?.averageDistance ?: 0.0,
                    averageSteps = stats?.averageSteps?.toInt() ?: 0,
                    longestStreak = stats?.longestStreak ?: 0,
                    isLarger = true,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Button(
                    onClick = onShareRecap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Rounded.Download, contentDescription = "Download & Share", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download & Share Recap", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
