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
                        "Share", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val width = (view.width * 0.8f).toInt().coerceIn(720, 1080)
                            val height = (width * 1.5f).toInt()
                            ComposeCaptureUtils.captureComposable(
                                view = view,
                                content = {
                                    Surface(color = MaterialTheme.colorScheme.background) {
                                        ShareLogCard(uiState = uiState)
                                    }
                                },
                                width = width,
                                height = height,
                                onBitmapCaptured = { bitmap ->
                                    ImageUtils.saveBitmapToGallery(context, bitmap)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Image saved to gallery!")
                                    }
                                }
                            )
                        }
                    }) {
                        Icon(Icons.Rounded.Download, "Download")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val width = (view.width * 0.8f).toInt().coerceIn(720, 1080)
                            val height = (width * 1.5f).toInt()
                            ComposeCaptureUtils.captureComposable(
                                view = view,
                                content = {
                                    Surface(color = MaterialTheme.colorScheme.background) {
                                        ShareLogCard(uiState = uiState)
                                    }
                                },
                                width = width,
                                height = height,
                                onBitmapCaptured = { bitmap ->
                                    ImageUtils.shareImage(context, bitmap, "Moonpage Log")
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Share", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Preview of the card
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .clip(RoundedCornerShape(24.dp))
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
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)
    val moodVisual = MoonIcons.Moods.getMoodVisual(uiState.selectedMood ?: 3, themeType, uiState.customMoods)
    
    val isActuallyDark = MaterialTheme.colorScheme.surface.let { (it.red * 0.299 + it.green * 0.587 + it.blue * 0.114) < 0.5 }
    val cardBg = if (isActuallyDark) Color(0xFF2C2C2E) else Color(0xFFF1F2ED)
    val itemBg = if (isActuallyDark) Color(0xFF3A3A3C) else Color.White
    val onCard = if (isActuallyDark) Color.White else Color(0xFF424242)
    val brandColor = Color(0xFF66BB6A)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBg)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mood Icon (Ảnh 1 style)
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
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Date Label
        Surface(
            color = onCard.copy(alpha = 0.05f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = date.format(formatter),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = onCard.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Activity Card with Sprout (Ảnh 1 style)
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = itemBg,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.selectedActivities.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No activities recorded", color = onCard.copy(alpha = 0.3f), fontSize = 14.sp)
                        }
                    } else {
                        val chunks = uiState.selectedActivities.chunked(6)
                        chunks.forEach { rowIds ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                            ) {
                                rowIds.forEach { activityId ->
                                    val activity = uiState.dynamicActivities.find { it.id == activityId }
                                    val icon = MoonIcons.getIconForActivity(activity?.name ?: "")
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(onCard.copy(alpha = 0.04f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (icon.drawableRes != null) {
                                            Image(
                                                painter = painterResource(id = icon.drawableRes),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else if (icon.vector != null) {
                                            Icon(icon.vector, null, tint = icon.color, modifier = Modifier.size(22.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Sprout Icon (Ảnh 1)
            Icon(
                imageVector = Icons.Rounded.Eco,
                contentDescription = null,
                tint = brandColor,
                modifier = Modifier.size(24.dp).padding(start = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Health Stats Card (Ảnh 2 style)
        Surface(
            color = itemBg.copy(alpha = 0.7f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Sleep
                StatRow(
                    icon = Icons.Rounded.Brightness2,
                    iconColor = Color(0xFFFFB74D),
                    label = formatSleep(uiState.sleepHours)
                )
                
                // Steps
                if (uiState.steps > 0) {
                    StatRow(
                        icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                        iconColor = Color(0xFF4FC3F7),
                        label = "${String.format("%, d", uiState.steps)} steps"
                    )
                }
                
                // Menstruation
                if (uiState.isMenstruation) {
                    StatRow(
                        icon = Icons.Rounded.WaterDrop,
                        iconColor = Color(0xFFF06292),
                        label = uiState.menstruationPhase ?: "On period"
                    )
                }
                
                // Calories/Distance (Optional)
                if (uiState.calories > 0) {
                    StatRow(
                        icon = Icons.Rounded.LocalFireDepartment,
                        iconColor = Color(0xFFFF8A65),
                        label = "${uiState.calories} kcal"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Note
        if (uiState.noteText.isNotBlank()) {
            Text(
                text = uiState.noteText,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                fontSize = 15.sp,
                color = onCard.copy(alpha = 0.8f),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Music Card
        if (!uiState.musicTitle.isNullOrBlank()) {
            Surface(
                color = itemBg,
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
                            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(52.dp).background(onCard.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.MusicNote, null, tint = onCard.copy(alpha = 0.3f))
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(uiState.musicTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onCard, maxLines = 1)
                        Text(uiState.artistName ?: "Unknown Artist", fontSize = 12.sp, color = onCard.copy(alpha = 0.6f), maxLines = 1)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Photos (Ảnh 2 style)
        if (uiState.dailyPhotos.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Daily Photos", 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = onCard,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.dailyPhotos.take(3).forEach { photoUrl ->
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Branding (Ảnh 1 style)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Eco,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = brandColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Moonpage",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = brandColor
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun StatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}

private fun formatSleep(hours: Float): String {
    val h = hours.toInt()
    val m = ((hours - h) * 60).toInt()
    return if (h > 0 || m > 0) "${h}h ${m}m" else "No sleep data"
}
