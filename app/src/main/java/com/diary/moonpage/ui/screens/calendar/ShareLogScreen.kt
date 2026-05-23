package com.diary.moonpage.ui.screens.calendar

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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diary.moonpage.R
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.core.util.MoonIcons
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import com.diary.moonpage.ui.screens.moment.components.DashedDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareLogRoute(
    dateString: String,
    onNavigateBack: () -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val graphicsLayer = rememberGraphicsLayer()

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
                                runCatching {
                                    graphicsLayer.toImageBitmap().asAndroidBitmap()
                                }.onSuccess { bitmap ->
                                    ImageUtils.saveBitmapToGallery(context, bitmap)
                                    snackbarHostState.showSnackbar("Saved to gallery")
                                }.onFailure { error ->
                                    snackbarHostState.showSnackbar("Save failed: ${error.message ?: "unknown error"}")
                                }
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
                            runCatching {
                                graphicsLayer.toImageBitmap().asAndroidBitmap()
                            }.onSuccess { bitmap ->
                                ImageUtils.shareImage(context, bitmap, "My Daily Log")
                            }.onFailure { error ->
                                snackbarHostState.showSnackbar("Share failed: ${error.message ?: "unknown error"}")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
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
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }
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
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF1F1ED))
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Header with Logo (Left) and Year (Right)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            }
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy")),
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mood Icon (Center)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Pill (Center)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dashed Line Divider
        DashedDivider(
            color = Color(0xFFD1D1CB),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Content Section (Activities, Note, Photos, Music) - LEFT ALIGNED
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // Activities (Max 6 per row)
            val allSelectedActivities = uiState.selectedActivities.mapNotNull { id ->
                uiState.dynamicActivities.find { it.id == id }
            }

            if (allSelectedActivities.isNotEmpty()) {
                allSelectedActivities.chunked(6).forEach { rowActivities ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowActivities.forEachIndexed { index, activity ->
                            val icon = MoonIcons.getIconForActivity(activity.name)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(icon.color.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (icon.drawableRes != null) {
                                    Image(
                                        painterResource(id = icon.drawableRes),
                                        null,
                                        modifier = Modifier.size(20.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Icon(
                                        icon.vector!!,
                                        null,
                                        modifier = Modifier.size(18.dp),
                                        tint = icon.color
                                    )
                                }
                            }
                            if (index < rowActivities.size - 1) Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Note Text
            if (!uiState.noteText.isNullOrBlank()) {
                Text(
                    text = uiState.noteText,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 15.sp,
                    color = Color(0xFF424242),
                    textAlign = TextAlign.Start,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Weather, Sleep, Steps Info
            val weather = uiState.suggestedWeather ?: uiState.existingLog?.let { 
                if (it.weather != null) com.diary.moonpage.domain.repository.WeatherData(it.weather!!, "", it.temperature ?: 0.0, "", "") else null
            }
            
            if (weather != null || uiState.sleepHours > 0 || uiState.steps > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (weather != null) {
                        ShareInfoItem(
                            icon = Icons.Rounded.WbSunny,
                            value = "${weather.temp.toInt()}°C",
                            label = weather.condition,
                            color = Color(0xFFFFB300)
                        )
                    }
                    if (uiState.sleepHours > 0) {
                        ShareInfoItem(
                            icon = Icons.Rounded.Bedtime,
                            value = String.format("%.1fh", uiState.sleepHours),
                            label = "Sleep",
                            color = Color(0xFF5C6BC0)
                        )
                    }
                    if (uiState.steps > 0) {
                        ShareInfoItem(
                            icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                            value = String.format("%,d", uiState.steps),
                            label = "Steps",
                            color = Color(0xFF66BB6A)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Photos Grid (Max 3 per row)
            val allPhotos = (uiState.dailyPhotos + uiState.momentPhotos).distinct()
            if (allPhotos.isNotEmpty()) {
                allPhotos.chunked(3).forEachIndexed { rowIndex, chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
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
                    if (rowIndex < (allPhotos.size - 1) / 3) Spacer(modifier = Modifier.height(8.dp))
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
        DashedDivider(
            color = Color(0xFFD1D1CB),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Footer Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    "MoonPage Daily Log",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
                Text(
                    text = "REF: ${uiState.date.toEpochDay()}${System.currentTimeMillis() % 10000}",
                    fontSize = 10.sp,
                    color = Color(0xFF9E9E9E)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(40.dp).alpha(0.6f)
            )
        }
    }
}

@Composable
private fun ShareInfoItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF424242)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF9E9E9E),
            fontWeight = FontWeight.Normal
        )
    }
}
