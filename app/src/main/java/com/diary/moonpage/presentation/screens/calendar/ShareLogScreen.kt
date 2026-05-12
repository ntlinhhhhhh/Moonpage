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
                    IconButton(onClick = {
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
                    }) {
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
                        containerColor = Color(0xFF5AA86B) // Green color from image
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
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFF7F7F5))
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
    
    // Formatting date: Monday, May 4
    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
    val dayOfMonth = date.dayOfMonth
    val dateText = "$dayOfWeek, $monthName $dayOfMonth"
    
    val moodVisual = MoonIcons.Moods.getMoodVisual(uiState.selectedMood ?: 3, themeType, uiState.customMoods)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F1ED)) // Slightly darker beige for the card background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mood Icon (Large)
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(moodVisual.color.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            if (moodVisual.drawableRes != null) {
                Image(
                    painter = painterResource(id = moodVisual.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date Pill
        Surface(
            color = Color.White.copy(alpha = 0.4f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = dateText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 13.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Dashed Line Divider
        com.diary.moonpage.presentation.components.moment.DashedDivider(
            color = Color(0xFFD1D1CB),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Activities White Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val activities = uiState.selectedActivities.mapNotNull { id ->
                    uiState.dynamicActivities.find { it.id == id }
                }
                
                if (activities.isEmpty()) {
                    Text("No activities recorded", color = Color.LightGray, fontSize = 12.sp)
                } else {
                    activities.chunked(4).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            chunk.forEach { activity ->
                                val icon = MoonIcons.getIconForActivity(activity.name)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(icon.color.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (icon.drawableRes != null) {
                                            Image(painterResource(id = icon.drawableRes), null, modifier = Modifier.size(28.dp))
                                        } else {
                                            Icon(icon.vector!!, null, modifier = Modifier.size(24.dp), tint = icon.color)
                                        }
                                    }
                                }
                            }
                        }
                        if (activities.size > 4) Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Another Dashed Divider
        com.diary.moonpage.presentation.components.moment.DashedDivider(
            color = Color(0xFFD1D1CB),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Note Text
        if (!uiState.noteText.isNullOrBlank()) {
            Text(
                text = uiState.noteText,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                fontSize = 15.sp,
                color = Color(0xFF424242),
                textAlign = TextAlign.Start,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Music Card
        if (!uiState.musicTitle.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
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
                            modifier = Modifier.size(52.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.MusicNote, null, tint = Color.Gray)
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
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Branding Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Rounded.Face, 
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF5AA86B)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "MoonPage",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF5AA86B)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Receipt ID / Timestamp at bottom
        Text(
            text = "REF: ${System.currentTimeMillis() / 1000}",
            fontSize = 10.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.padding(bottom = 8.dp).alpha(0.6f)
        )
    }
}
