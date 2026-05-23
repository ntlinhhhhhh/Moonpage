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
    val existingLog = uiState.existingLog
    val moodVisual = MoonIcons.Moods.getMoodVisual(
        uiState.selectedMood ?: existingLog?.baseMoodId ?: 3,
        uiState.themeType,
        uiState.customMoods
    )
    val activityNames = uiState.selectedActivities.mapNotNull { id ->
        uiState.dynamicActivities.find { it.id == id }?.name
    }
    val photos = (uiState.dailyPhotos + uiState.momentPhotos).distinct()
    val noteSnippet = uiState.noteText.takeIf { it.isNotBlank() } ?: existingLog?.note
    val musicRecord = when {
        !uiState.musicTitle.isNullOrBlank() && !uiState.artistName.isNullOrBlank() ->
            "${uiState.musicTitle} - ${uiState.artistName}"
        !uiState.musicTitle.isNullOrBlank() -> uiState.musicTitle
        else -> existingLog?.musicRecord
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MoonTheme.customColors.logCardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        com.diary.moonpage.ui.screens.calendar.components.DayDetailArea(
            date = uiState.date,
            moodIcon = moodVisual.vector,
            moodDrawable = moodVisual.drawableRes,
            moodColor = moodVisual.color,
            moodLabel = moodVisual.name,
            noteSnippet = noteSnippet,
            activityNames = activityNames,
            dailyPhotos = photos,
            sleepHours = existingLog?.sleepHours ?: uiState.sleepHours.toDouble(),
            isMenstruation = uiState.isMenstruation,
            steps = existingLog?.steps ?: uiState.steps,
            musicRecord = musicRecord,
            weather = existingLog?.weather,
            temperature = existingLog?.temperature
        )
    }
}
