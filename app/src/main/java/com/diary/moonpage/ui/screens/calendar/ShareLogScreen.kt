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

import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Divider

/**
 * Stateful Component
 */
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

    ShareLogScreen(
        uiState = uiState,
        moodVisual = moodVisual,
        snackbarHostState = snackbarHostState,
        graphicsLayer = graphicsLayer,
        onSaveClick = {
            scope.launch {
                try {
                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                    ImageUtils.saveBitmapToGallery(context, bitmap)
                    snackbarHostState.showSnackbar("Saved to gallery!")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Save failed: ${e.message}")
                }
            }
        },
        onNavigateBack = onNavigateBack
    )
}

/**
 * Stateless Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareLogScreen(
    uiState: DailyLogUiState,
    moodVisual: com.diary.moonpage.core.util.MoonIcon,
    snackbarHostState: SnackbarHostState,
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val themeColor = moodVisual.color

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Share Log", 
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
                    IconButton(
                        enabled = !uiState.isLoading,
                        onClick = onSaveClick
                    ) {
                        Icon(Icons.Rounded.Download, "Save", modifier = Modifier.size(24.dp), tint = themeColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F8F8)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Capture Area
                Card(
                    modifier = Modifier
                        .width(340.dp)
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        },
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    ShareCardContent(uiState, moodVisual)
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                // Share options
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShareActionButton(Icons.Rounded.ContentCopy, "Copy") {
                         // TODO: Implement copy to clipboard
                    }
                    ShareActionButton(Icons.Rounded.Share, "Share") {
                         // TODO: Implement native share
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }

            MoonSnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShareCardContent(uiState: DailyLogUiState, moodVisual: com.diary.moonpage.core.util.MoonIcon) {
    val date = uiState.date
    val themeColor = moodVisual.color
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date Header
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = themeColor,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mood Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(themeColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (moodVisual.drawableRes != null) {
                Image(
                    painter = painterResource(moodVisual.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
            } else if (moodVisual.vector != null) {
                Icon(
                    imageVector = moodVisual.vector,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Feeling ${moodVisual.name}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Activities
        if (uiState.selectedActivities.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                uiState.selectedActivities.forEach { id ->
                    val activity = uiState.dynamicActivities.find { it.id == id }
                    if (activity != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = activity.name,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF616161)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Note
        if (uiState.noteText.isNotBlank()) {
            Text(
                text = "“${uiState.noteText}”",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(0xFF757575),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Photo
        if (!uiState.dailyPhotos.isNullOrEmpty()) {
            AsyncImage(
                model = uiState.dailyPhotos.first(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFEEEEEE))
        Spacer(modifier = Modifier.height(16.dp))
        
        // Brand
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WbTwilight, null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.width(8.dp))
            Text("MOONPAGE", style = MaterialTheme.typography.labelSmall, color = Color.LightGray, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun ShareActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(56.dp).shadow(2.dp, CircleShape)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color(0xFF424242))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF757575))
    }
}
