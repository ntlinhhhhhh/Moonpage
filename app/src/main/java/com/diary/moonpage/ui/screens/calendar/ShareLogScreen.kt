package com.diary.moonpage.ui.screens.calendar

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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonPageTheme
import com.diary.moonpage.core.theme.MoonTheme
import com.diary.moonpage.core.util.ComposeCaptureUtils
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.core.util.MoonIcons
import kotlinx.coroutines.launch
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

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
    val view = LocalView.current
    val compositionContext = rememberCompositionContext()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val graphicsLayer = rememberGraphicsLayer()
    val savedToGalleryMessage = stringResource(R.string.share_saved_to_gallery)
    val logImageTitle = stringResource(R.string.share_log_image_title)

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
                        stringResource(R.string.share_log_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, stringResource(R.string.back), modifier = Modifier.size(20.dp), tint = Color(0xFF757575))
                    }
                },
                actions = {
                    IconButton(
                        enabled = !uiState.isLoading,
                        onClick = {
                            scope.launch {
                                try {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    ImageUtils.saveBitmapToGallery(context, bitmap)
                                    snackbarHostState.showSnackbar(savedToGalleryMessage)
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(context.getString(R.string.share_save_failed, e.message ?: ""))
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Download, stringResource(R.string.share_download), tint = Color(0xFF757575))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Large Share Button (No icon)
                Button(
                    enabled = !uiState.isLoading,
                    onClick = {
                        scope.launch {
                            try {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                ImageUtils.shareImage(context, bitmap, logImageTitle)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(context.getString(R.string.share_failed, e.message ?: ""))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp), // Larger height
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Text(stringResource(R.string.share), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        },
        snackbarHost = { MoonSnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
    val themeColor = MaterialTheme.colorScheme.primary
    
    val isDark = MoonTheme.customColors.isDark
    val cardBg = if (isDark) Color(0xFF2C2C2C) else Color(0xFFF1F1ED)
    val textColor = if (isDark) Color(0xFFEEEEEE) else Color(0xFF424242)
    val secondaryTextColor = if (isDark) Color(0xFFAAAAAA) else Color(0xFF9E9E9E)
    val pillBg = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f)
    val pillText = if (isDark) Color(0xFFDDDDDD) else Color(0xFF616161)
    val dividerColor = if (isDark) Color(0xFF444444) else Color(0xFFD1D1CB)
    val musicCardBg = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.8f)
    val unknownArtist = stringResource(R.string.daily_log_unknown_artist)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start 
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
                color = secondaryTextColor,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Mood Icon (Centered)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(moodVisual.color.copy(alpha = 0.8f))
                .align(Alignment.CenterHorizontally),
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
        
        // Date Pill (Centered)
        Surface(
            color = pillBg,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = dateText,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                fontSize = 14.sp,
                color = pillText,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Dashed Line Divider
        com.diary.moonpage.ui.screens.moment.components.DashedDivider(
            color = dividerColor,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Content Section (Activities, Note, Photos, Music)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start // Left aligned content
        ) {
            // Activities (Max 6, Left Aligned)
            val activities = uiState.selectedActivities.mapNotNull { id ->
                uiState.dynamicActivities.find { it.id == id }
            }.take(6)
            
            if (activities.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activities.forEachIndexed { index, activity ->
                        val icon = MoonIcons.getIconForActivity(activity.name)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(icon.color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (icon.drawableRes != null) {
                                Image(painterResource(id = icon.drawableRes), null, modifier = Modifier.size(22.dp))
                            } else {
                                Icon(icon.vector!!, null, modifier = Modifier.size(20.dp), tint = icon.color)
                            }
                        }
                        if (index < activities.size - 1) Spacer(modifier = Modifier.width(10.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Note Text (Left Aligned)
            if (!uiState.noteText.isNullOrBlank()) {
                Text(
                    text = uiState.noteText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    fontSize = 15.sp,
                    color = textColor,
                    textAlign = TextAlign.Start,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Music Card (Left Aligned, below Note)
            if (!uiState.musicTitle.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = musicCardBg
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
                                color = textColor,
                                maxLines = 1
                            )
                            Text(
                                text = uiState.artistName ?: unknownArtist,
                                fontSize = 12.sp,
                                color = secondaryTextColor,
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Photos Grid (Max 3 per row, Left Aligned, below Music)
            if (uiState.dailyPhotos.isNotEmpty()) {
                val photos = uiState.dailyPhotos
                photos.chunked(3).forEachIndexed { rowIndex, chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        chunk.forEachIndexed { colIndex, photoUrl ->
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            if (colIndex < chunk.size - 1) Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    if (rowIndex < (photos.size - 1) / 3) Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Another Dashed Divider
        com.diary.moonpage.ui.screens.moment.components.DashedDivider(
            color = dividerColor,
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
                    stringResource(R.string.share_log_footer),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
                Text(
                    text = stringResource(R.string.share_log_reference, System.currentTimeMillis() / 1000),
                    fontSize = 10.sp,
                    color = secondaryTextColor
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
