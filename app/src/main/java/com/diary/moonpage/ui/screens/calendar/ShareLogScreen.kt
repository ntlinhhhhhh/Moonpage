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
import androidx.compose.ui.text.font.FontStyle
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
import com.diary.moonpage.core.theme.MoonTheme

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
    val isDark = MoonTheme.customColors.isDark

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
                        fontStyle = FontStyle.Normal,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, "Back", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
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
                                }.onFailure { error ->
                                    snackbarHostState.showSnackbar("Save failed: ${error.message ?: "unknown error"}")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Download, "Save", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
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
                    Text("Share", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal, color = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
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
                        .background(Color.Transparent) // Transparent outside the card
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
    val isDark = MoonTheme.customColors.isDark

    // Formatting date: Monday, May 4
    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
    val dayOfMonth = date.dayOfMonth
    val dateText = "$dayOfWeek, $monthName $dayOfMonth"

    val moodVisual = MoonIcons.Moods.getMoodVisual(uiState.selectedMood ?: 3, themeType, uiState.customMoods)
    val themeColor = moodVisual.color
    
    val cardBgColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFF1F1ED)
    val textColor = if (isDark) Color(0xFFEEEEEE) else Color(0xFF424242)
    val secondaryTextColor = if (isDark) Color(0xFFAAAAAA) else Color(0xFF9E9E9E)
    val dividerColor = if (isDark) Color(0xFF424242) else Color(0xFFD1D1CB)
    val pillBgColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f)
    val pillTextColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFF616161)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBgColor)
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
                    fontStyle = FontStyle.Normal,
                    fontSize = 18.sp,
                    color = themeColor
                )
            }
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy")),
                fontSize = 14.sp,
                fontStyle = FontStyle.Normal,
                color = secondaryTextColor,
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
                color = pillBgColor,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = dateText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Normal,
                    color = pillTextColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dashed Line Divider
        DashedDivider(
            color = dividerColor,
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
                                    .background(icon.color.copy(alpha = if (isDark) 0.2f else 0.1f)),
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
                    fontStyle = FontStyle.Normal,
                    color = textColor,
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
                            color = Color(0xFFFFB300),
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor
                        )
                    }
                    if (uiState.sleepHours > 0) {
                        ShareInfoItem(
                            icon = Icons.Rounded.Bedtime,
                            value = String.format("%.1fh", uiState.sleepHours),
                            label = "Sleep",
                            color = Color(0xFF5C6BC0),
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor
                        )
                    }
                    if (uiState.steps > 0) {
                        ShareInfoItem(
                            icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                            value = String.format("%,d", uiState.steps),
                            label = "Steps",
                            color = Color(0xFF66BB6A),
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor
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
                    color = pillBgColor
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
                                modifier = Modifier.size(48.dp).background(if (isDark) Color(0xFF424242) else Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.MusicNote, null, tint = secondaryTextColor, modifier = Modifier.size(24.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = uiState.musicTitle,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 14.sp,
                                color = textColor,
                                maxLines = 1
                            )
                            Text(
                                text = uiState.artistName ?: "Unknown Artist",
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Normal,
                                color = secondaryTextColor,
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
            color = dividerColor,
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
                    fontStyle = FontStyle.Normal,
                    color = themeColor
                )
                Text(
                    text = "REF: ${uiState.date.toEpochDay()}${System.currentTimeMillis() % 10000}",
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Normal,
                    color = secondaryTextColor
                )
            }

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(40.dp).alpha(if (isDark) 0.4f else 0.6f)
            )
        }
    }
}

@Composable
private fun ShareInfoItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    textColor: Color,
    secondaryTextColor: Color
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
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                color = textColor
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontStyle = FontStyle.Normal,
            color = secondaryTextColor,
            fontWeight = FontWeight.Normal
        )
    }
}
