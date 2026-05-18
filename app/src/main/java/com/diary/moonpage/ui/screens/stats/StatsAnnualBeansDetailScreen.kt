package com.diary.moonpage.ui.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.ui.screens.stats.components.*

// Imports for graphicsLayer capture & saving
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import kotlinx.coroutines.launch

@Composable
fun StatsAnnualBeansDetailRoute(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StatsAnnualBeansDetailScreen(
        uiState = uiState,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsAnnualBeansDetailScreen(
    uiState: StatisticsUiState,
    onBack: () -> Unit
) {
    val stats = uiState.stats
    val flatGridList = stats?.yearlyMoodGrid ?: stats?.moodFlow ?: emptyList()
    
    var showRecapDetail by remember { mutableStateOf(false) }

    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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
                        IconButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                        ImageUtils.saveBitmapToGallery(context, bitmap)
                                        snackbarHostState.showSnackbar("Saved to gallery!")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Save failed: ${e.message}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.Download, "Download", tint = Color(0xFF757575))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (showRecapDetail) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    ImageUtils.shareImage(context, bitmap, "My Yearly Recap")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Share failed: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("Share", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        },
        snackbarHost = { MoonSnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (!showRecapDetail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Capture Area for the YearlyRecapCard
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }
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
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
