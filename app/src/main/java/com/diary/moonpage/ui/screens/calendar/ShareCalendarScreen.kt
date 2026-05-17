package com.diary.moonpage.ui.screens.calendar

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.theme.getThemeShades
import kotlinx.coroutines.launch
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.style.TextAlign
import com.diary.moonpage.core.theme.MoonTheme

import androidx.compose.ui.draw.shadow
import com.diary.moonpage.ui.screens.calendar.components.CalendarGrid

/**
 * Stateful Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCalendarRoute(
    yearMonthString: String,
    onNavigateBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val graphicsLayer = rememberGraphicsLayer()

    val yearMonth = remember(yearMonthString) {
        try {
            YearMonth.parse(yearMonthString)
        } catch (e: Exception) {
            YearMonth.now()
        }
    }

    // Sync VM with the month we want to share
    LaunchedEffect(yearMonth) {
        viewModel.onEvent(CalendarUiEvent.OnMonthChanged(yearMonth))
    }

    ShareCalendarScreen(
        uiState = uiState,
        yearMonth = yearMonth,
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
fun ShareCalendarScreen(
    uiState: CalendarUiState,
    yearMonth: YearMonth,
    snackbarHostState: SnackbarHostState,
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedRatio by remember { mutableStateOf("1:1") }
    val themeColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Share", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, "Back", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                Box(
                    modifier = Modifier
                        .then(if (selectedRatio == "1:1") Modifier.size(340.dp) else Modifier.width(340.dp).aspectRatio(9f/16f))
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }
                        .background(Color.White, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ShareCalendarCardContent(uiState, yearMonth)
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Ratio Selector
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RatioButton("1:1", selectedRatio == "1:1") { selectedRatio = "1:1" }
                    RatioButton("9:16", selectedRatio == "9:16") { selectedRatio = "9:16" }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Social Actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShareSocialButton(Icons.Rounded.ContentCopy, "Copy")
                    ShareSocialButton(Icons.Rounded.Camera, "Stories")
                    ShareSocialButton(Icons.Rounded.MoreHoriz, "More")
                }

                Spacer(modifier = Modifier.height(48.dp))
            }

            MoonSnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun ShareCalendarCardContent(uiState: CalendarUiState, yearMonth: YearMonth) {
    // Implement the actual UI previously in ShareCalendarScreen
    // Keeping it brief for this refactor pass
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Miniature Calendar Grid
        com.diary.moonpage.ui.screens.calendar.components.CalendarGrid(
            pageYearMonth = yearMonth,
            selectedDate = null,
            dailyLogs = uiState.dailyLogs,
            selectedFilter = null,
            dynamicActivities = emptyList(),
            themeType = uiState.themeType,
            onDateSelected = {},
            isReadOnly = true
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Brand
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WbTwilight, null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.width(8.dp))
            Text("MOONPAGE", style = MaterialTheme.typography.labelSmall, color = Color.LightGray, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun RatioButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ShareSocialButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(56.dp).shadow(2.dp, CircleShape)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color(0xFF424242))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
