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
import androidx.compose.ui.draw.alpha
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
import java.time.format.TextStyle
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
    val compositionContext = rememberCompositionContext()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = androidx.compose.ui.platform.LocalDensity.current

    LaunchedEffect(dateString) {
        viewModel.setInitialDate(LocalDate.parse(dateString))
    }

    val moodVisual = MoonIcons.Moods.getMoodVisual(uiState.selectedMood ?: 3, uiState.themeType, uiState.customMoods)
    val themeColor = moodVisual.color

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Share", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, "Back", modifier = Modifier.size(20.dp), tint = Color(0xFF757575))
                    }
                },
                actions = {
                    // Download icon in Top Bar
                    IconButton(
                        enabled = !uiState.isLoading,
                        onClick = {
                            scope.launch {
                                val width = 1080
                                ComposeCaptureUtils.captureComposable(
                                    view = view,
                                    parentContext = compositionContext,
                                    content = {
                                        Box(
                                            modifier = Modifier
                                                .width(with(density) { 1080.toDp() })
                                                .background(Color(0xFFF7F7F5))
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            ShareLogCard(uiState = uiState)
                                        }
                                    },
                                    width = width,
                                    onBitmapCaptured = { bitmap ->
                                        ImageUtils.saveBitmapToGallery(context, bitmap)
                                    }
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Download, "Save", tint = Color(0xFF757575))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF7F7F5)
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                color = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                Button(
                    enabled = !uiState.isLoading,
                    onClick = {
                        scope.launch {
                            val width = 1080
                            ComposeCaptureUtils.captureComposable(
                                view = view,
                                parentContext = compositionContext,
                                content = {
                                    Box(
                                        modifier = Modifier
                                            .width(with(density) { 1080.toDp() })
                                            .background(Color(0xFFF7F7F5))
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        ShareLogCard(uiState = uiState)
                                    }
                                },
                                width = width,
                                onBitmapCaptured = { bitmap ->
                                    ImageUtils.shareImage(context, bitmap, "My Daily Log")
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Share", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF7F7F5)
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = themeColor)
            }
        } else {
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
                        .padding(horizontal = 24.dp)
                        .width(360.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF1F1ED))
                ) {
                    ShareLogCard(uiState = uiState)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ShareLogCard(uiState: DailyLogUiState) {
    val date = uiState.date
    val themeType = uiState.themeType
    
    // Formatting date: Monday, May 4
    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
    val dayOfMonth = date.dayOfMonth
    val dateText = "$dayOfWeek, $monthName $dayOfMonth"
    
    val moodVisual = MoonIcons.Moods.getMoodVisual(uiState.selectedMood ?: 3, themeType, uiState.customMoods)
    val themeColor = moodVisual.color
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F1ED)) // Slightly darker beige for the card background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "MoonPage",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = themeColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy")),
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E),
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Mood Icon (Smaller as requested)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(themeColor.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            if (moodVisual.drawableRes != null) {
                Image(
                    painter = painterResource(id = moodVisual.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date Pill
        Surface(
            color = Color.White.copy(alpha = 0.6f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = dateText,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                fontSize = 14.sp,
                color = Color(0xFF616161),
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Dashed Line Divider
        com.diary.moonpage.presentation.components.moment.DashedDivider(
            color = Color(0xFFD1D1CB),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Content Section (Activities, Note, Photos, Music)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Activities (Max 6)
            val activities = uiState.selectedActivities.mapNotNull { id ->
                uiState.dynamicActivities.find { it.id == id }
            }.take(6)
            
            if (activities.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activities.forEachIndexed { index, activity ->
                        val icon = MoonIcons.getIconForActivity(activity.name)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(icon.color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (icon.drawableRes != null) {
                                Image(painterResource(id = icon.drawableRes), null, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(icon.vector!!, null, modifier = Modifier.size(22.dp), tint = icon.color)
                            }
                        }
                        if (index < activities.size - 1) Spacer(modifier = Modifier.width(12.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Note Text
            if (!uiState.noteText.isNullOrBlank()) {
                Text(
                    text = uiState.noteText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    fontSize = 15.sp,
                    color = Color(0xFF424242),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Photos Grid (Max 3 per row)
            if (uiState.dailyPhotos.isNotEmpty()) {
                val photos = uiState.dailyPhotos
                photos.chunked(3).forEachIndexed { rowIndex, chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        chunk.forEachIndexed { colIndex, photoUrl ->
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            if (colIndex < chunk.size - 1) Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    if (rowIndex < (photos.size - 1) / 3) Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Music Card
            if (!uiState.musicTitle.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.albumArtUrl != null) {
                            AsyncImage(
                                model = uiState.albumArtUrl,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(48.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.MusicNote, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = uiState.musicTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF424242),
                                maxLines = 1
                            )
                            Text(
                                text = uiState.artistName ?: "Unknown Artist",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E),
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Another Dashed Divider
        com.diary.moonpage.presentation.components.moment.DashedDivider(
            color = Color(0xFFD1D1CB),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Footer Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "MoonPage Daily Log",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
                Text(
                    text = "REF: ${System.currentTimeMillis() / 1000}",
                    fontSize = 10.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
            
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(36.dp).alpha(0.4f)
            )
        }
    }
}
