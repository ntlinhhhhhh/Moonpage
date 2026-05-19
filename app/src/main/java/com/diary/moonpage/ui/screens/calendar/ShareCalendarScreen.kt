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
import androidx.compose.ui.res.stringResource
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
    val savedToGalleryMessage = stringResource(R.string.share_saved_to_gallery)
    val calendarImageTitle = stringResource(R.string.share_calendar_image_title)
    
    var selectedRatio by remember { mutableStateOf("1:1") }

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

    val themeColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.share_calendar_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, stringResource(R.string.back), modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
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
                        Icon(Icons.Rounded.Download, stringResource(R.string.share_download), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
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
                Button(
                    enabled = !uiState.isLoading,
                    onClick = {
                        scope.launch {
                            try {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                ImageUtils.shareImage(context, bitmap, calendarImageTitle)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(context.getString(R.string.share_failed, e.message ?: ""))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Ratio Selector
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(stringResource(R.string.share_ratio), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                
                RatioToggleItem(
                    label = "1:1",
                    isSelected = selectedRatio == "1:1",
                    onClick = { selectedRatio = "1:1" },
                    themeColor = themeColor
                )
                
                RatioToggleItem(
                    label = "9:16",
                    isSelected = selectedRatio == "9:16",
                    onClick = { selectedRatio = "9:16" },
                    themeColor = themeColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Card Wrapper - matches the background color and shadow from images
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .aspectRatio(if (selectedRatio == "1:1") 1f else 9f/16f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (MoonTheme.customColors.isDark) Color(0xFF2C2C2C) else Color(0xFFF1F1ED)) // Lighter dark gray for card in Dark Mode
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    },
                contentAlignment = Alignment.Center
            ) {
                ShareCalendarContent(
                    yearMonth = yearMonth,
                    dailyLogs = uiState.dailyLogs,
                    themeType = uiState.themeType,
                    isSquare = selectedRatio == "1:1"
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RatioToggleItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    themeColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) themeColor else Color(0xFFF0F0F0))
                .padding(4.dp)
        ) {
            if (isSelected) {
                Box(modifier = Modifier.fillMaxSize().background(if (MoonTheme.customColors.isDark) Color.Black else Color.White, CircleShape))
            }
        }
        Text(
            text = label,
            fontSize = 15.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun ShareCalendarContent(
    yearMonth: YearMonth,
    dailyLogs: Map<LocalDate, com.diary.moonpage.domain.model.DailyLog>,
    themeType: MoonThemeType,
    isSquare: Boolean
) {
    val monthName = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
    val themeColor = MaterialTheme.colorScheme.primary
    val isDark = MoonTheme.customColors.isDark
    
    val textColor = if (isDark) Color(0xFFEEEEEE) else Color(0xFF424242)
    val secondaryTextColor = if (isDark) Color(0xFFAAAAAA) else Color(0xFF9E9E9E)
    val headerColor = if (isDark) Color(0xFF888888) else Color(0xFFBDBDBD)
    val emptyCellColor = if (isDark) Color(0xFF505457) else Color(0xFFEDEDE9).copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isSquare) 20.dp else 24.dp), // Slightly reduced padding to give more space
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = monthName,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = textColor
        )
        
        Spacer(modifier = Modifier.height(if (isSquare) 12.dp else 24.dp))
        
        // Days of week header
        Row(modifier = Modifier.fillMaxWidth()) {
            val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = headerColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(if (isSquare) 4.dp else 12.dp))
        
        // Calendar Grid
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfMonth = yearMonth.atDay(1)
        val firstDayOffset = firstDayOfMonth.dayOfWeek.value % 7
        val totalCells = firstDayOffset + daysInMonth
        val rows = 6 // Standardized to 6 rows

        Column(verticalArrangement = Arrangement.spacedBy(if (isSquare) 0.dp else 8.dp)) {
            for (rowIndex in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth().height(if (isSquare) 40.dp else 50.dp)) {
                    for (colIndex in 0 until 7) {
                        val cellIndex = rowIndex * 7 + colIndex
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellIndex in firstDayOffset until totalCells) {
                                val dayNum = cellIndex - firstDayOffset + 1
                                val date = yearMonth.atDay(dayNum)
                                val log = dailyLogs[date]
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Mood Circle
                                    Box(
                                        modifier = Modifier
                                            .size(if (isSquare) 28.dp else 32.dp) // Reduced size from 36/44
                                            .clip(CircleShape)
                                            .background(if (log != null) {
                                                val mv = MoonIcons.Moods.getMoodVisual(log.baseMoodId, themeType)
                                                mv.color
                                            } else {
                                                emptyCellColor
                                            }),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (log != null) {
                                            val mv = MoonIcons.Moods.getMoodVisual(log.baseMoodId, themeType)
                                            if (mv.drawableRes != null) {
                                                Image(
                                                    painter = painterResource(id = mv.drawableRes),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(0.65f)
                                                )
                                            }
                                        }
                                    }
                                    
                                    if (!isSquare) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        // Day number - only shown in 9:16
                                        Text(
                                            text = dayNum.toString(),
                                            fontSize = 9.sp,
                                            color = secondaryTextColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (!isSquare) {
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Footer Logo - matching DailyBean style
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "MoonPage",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = themeColor
            )
        }
    }
}
