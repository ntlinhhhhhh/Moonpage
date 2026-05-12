package com.diary.moonpage.presentation.screens.calendar

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonTheme
import com.diary.moonpage.core.util.ComposeCaptureUtils
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.core.util.MoonIcons
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareLogScreen(
    dateString: String,
    onNavigateBack: () -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(dateString) {
        viewModel.setInitialDate(LocalDate.parse(dateString))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Share Log", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                color = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Download Button
                    Button(
                        onClick = {
                            scope.launch {
                                val width = 1080
                                ComposeCaptureUtils.captureComposable(
                                    view = view,
                                    content = {
                                        Box(
                                            modifier = Modifier
                                                .width(1080.dp / 2.625f) // Adjust for capturing
                                                .background(Color(0xFFF7F7F5)) // Premium beige background
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            ShareLogCard(uiState = uiState)
                                        }
                                    },
                                    width = width,
                                    onBitmapCaptured = { bitmap ->
                                        ImageUtils.saveBitmapToGallery(context, bitmap)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Image saved to gallery!")
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(Icons.Rounded.Download, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share Button
                    Button(
                        onClick = {
                            scope.launch {
                                val width = 1080
                                ComposeCaptureUtils.captureComposable(
                                    view = view,
                                    content = {
                                        Box(
                                            modifier = Modifier
                                                .width(1080.dp / 2.625f)
                                                .background(Color(0xFFF7F7F5))
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            ShareLogCard(uiState = uiState)
                                        }
                                    },
                                    width = width,
                                    onBitmapCaptured = { bitmap ->
                                        ImageUtils.shareImage(context, bitmap, "My Moonpage Log")
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(Icons.Rounded.Share, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF7F7F5) // Use the same beige background for the screen
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Preview of the card with shadow
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .width(360.dp),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 12.dp,
                color = Color.White
            ) {
                ShareLogCard(uiState = uiState)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ShareLogCard(uiState: DailyLogUiState) {
    val date = uiState.date
    val themeType = uiState.themeType
    
    // Formatting date
    val dayFormatter = DateTimeFormatter.ofPattern("d", Locale.ENGLISH)
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
    val dateBadgeText = "${date.format(dayFormatter)} ${date.format(dayOfWeekFormatter)}"
    
    val moodVisual = MoonIcons.Moods.getMoodVisual(uiState.selectedMood ?: 3, themeType, uiState.customMoods)
    
    val metricBg = Color(0xFFF7F8FA)
    val onSurface = Color(0xFF424242)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Mood and Date
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Min)
        ) {
            // Mood Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(moodVisual.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (moodVisual.drawableRes != null) {
                    Image(
                        painter = painterResource(id = moodVisual.drawableRes),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date Badge
            Surface(
                color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = dateBadgeText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.width(20.dp))
        
        // Vertical Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(IntrinsicSize.Min)
                .background(Color(0xFFEEEEEE))
                .padding(vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.width(20.dp))
        
        // Right Column: Metrics, Music, Photos
        Column(modifier = Modifier.weight(1f)) {
            // Metrics Section
            Surface(
                color = metricBg,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Sleep
                    MetricItem(
                        icon = Icons.Rounded.NightsStay,
                        iconColor = Color(0xFFFFC107),
                        text = "${(uiState.sleepHours).toInt()}h ${( (uiState.sleepHours % 1) * 60).toInt()}m"
                    )
                    
                    // Primary Activity (if any)
                    if (uiState.selectedActivities.isNotEmpty()) {
                        val firstActivityId = uiState.selectedActivities.first()
                        val activity = uiState.dynamicActivities.find { it.id == firstActivityId }
                        MetricItem(
                            icon = Icons.Rounded.Whatshot,
                            iconColor = Color(0xFFFF5722),
                            text = activity?.name ?: "Activity"
                        )
                    }

                    // Menstruation
                    if (uiState.isMenstruation) {
                        MetricItem(
                            icon = Icons.Rounded.WaterDrop,
                            iconColor = Color(0xFFF06292),
                            text = uiState.menstruationPhase ?: "On cycle"
                        )
                    }
                    
                    // Steps
                    MetricItem(
                        icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                        iconColor = Color(0xFF42A5F5),
                        text = "%,d steps".format(uiState.steps)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Music Card
            if (!uiState.musicTitle.isNullOrBlank()) {
                Surface(
                    color = metricBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.albumArtUrl != null) {
                            AsyncImage(
                                model = uiState.albumArtUrl,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(52.dp).background(Color(0xFFF06292).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.MusicNote, null, tint = Color(0xFFF06292))
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                uiState.musicTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = onSurface,
                                maxLines = 1
                            )
                            Text(
                                uiState.artistName ?: "Unknown Artist",
                                fontSize = 13.sp,
                                color = onSurface.copy(alpha = 0.6f),
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Photos Section
            if (uiState.dailyPhotos.isNotEmpty()) {
                Text(
                    "Daily Photos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.dailyPhotos.take(2).forEach { photoPath ->
                        AsyncImage(
                            model = photoPath,
                            contentDescription = null,
                            modifier = Modifier
                                .size(if (uiState.dailyPhotos.size > 1) 100.dp else 160.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    iconColor: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF424242),
            fontWeight = FontWeight.Medium
        )
    }
}
