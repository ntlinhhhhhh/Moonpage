package com.diary.moonpage.ui.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.data.remote.dto.stats.MoodFlowDto
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import com.diary.moonpage.ui.screens.stats.components.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun StatsAnnualBeansDetailRoute(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsAnnualBeansDetailScreen(
        uiState = uiState,
        onBack = onBack
    )
}

// ────────────────────────────────────────────────────────────────
// Main Screen
// ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsAnnualBeansDetailScreen(
    uiState: StatisticsUiState,
    onBack: () -> Unit
) {
    val stats = uiState.stats
    // Prefer yearlyMoodGrid, fall back to moodFlow
    val moodData: List<MoodFlowDto> = stats?.yearlyMoodGrid ?: stats?.moodFlow ?: emptyList()

    var showRecapDetail by remember { mutableStateOf(false) }
    // Tab state inside detail view: 0 = Entire year, 1 = By month
    var selectedTab by remember { mutableStateOf(0) }

    val graphicsLayerEntire = rememberGraphicsLayer()
    val graphicsLayerMonth = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (!showRecapDetail) {
                            stringResource(id = com.diary.moonpage.R.string.year_in_beans)
                        } else {
                            "Your Recap"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (showRecapDetail) showRecapDetail = false else onBack()
                        }
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (showRecapDetail) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val layer = if (selectedTab == 0) graphicsLayerEntire else graphicsLayerMonth
                                        val bitmap = layer.toImageBitmap().asAndroidBitmap()
                                        ImageUtils.saveBitmapToGallery(context, bitmap)
                                        snackbarHostState.showSnackbar("Saved to gallery!")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Save failed: ${e.message}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.Download, "Download", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (showRecapDetail) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val layer = if (selectedTab == 0) graphicsLayerEntire else graphicsLayerMonth
                                    val bitmap = layer.toImageBitmap().asAndroidBitmap()
                                    val roundedBitmap = ImageUtils.applyRoundedCorners(bitmap, 32.dp.value * context.resources.displayMetrics.density)
                                    ImageUtils.shareImage(context, roundedBitmap, "My Yearly Recap")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Share failed: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Share", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
            }
        },
        snackbarHost = { MoonSnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (!showRecapDetail) {
            // ── Landing card ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                YearInMoonpageMiniatureCard(
                    year = uiState.selectedYear,
                    themeType = uiState.themeType,
                    onDetailClick = { showRecapDetail = true }
                )
            }
        } else {
            // ── Recap Detail with Tabs ─────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── Tab Row ──
                RecapTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                // ── Content per tab ──
                when (selectedTab) {
                    0 -> EntireYearContent(
                        year = uiState.selectedYear,
                        moodData = moodData,
                        themeType = uiState.themeType,
                        graphicsLayer = graphicsLayerEntire
                    )
                    1 -> ByMonthContent(
                        year = uiState.selectedYear,
                        moodData = moodData,
                        themeType = uiState.themeType,
                        graphicsLayer = graphicsLayerMonth
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// Tab Row
// ────────────────────────────────────────────────────────────────

@Composable
private fun RecapTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        listOf("Entire year", "By month").forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            val color = if (isSelected) primary else inactive
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    color = color,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(56.dp)
                            .background(primary, RoundedCornerShape(1.dp))
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// Entire Year Tab
// ────────────────────────────────────────────────────────────────

@Composable
private fun EntireYearContent(
    year: Int,
    moodData: List<MoodFlowDto>,
    themeType: MoonThemeType,
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer
) {
    val moodMap = remember(moodData) { moodData.associateBy { it.date } }
    val emptyColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.13f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Year heading
        Text(
            text = "$year",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
        )

        // Capture area — explicit background so download/share works in both light & dark mode
        val captureBackground = MaterialTheme.colorScheme.background

        // Label geometry (fixed)
        val dayLabelWidth = 18.dp
        val labelToDotsGap = 4.dp
        val outerHorizPadding = 12.dp // padding inside the capture box

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(captureBackground)
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
                .padding(horizontal = outerHorizPadding, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Measure available width and fill it with the grid
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val available = maxWidth - dayLabelWidth - labelToDotsGap
                // monthGap stays proportional: use ~30 % of cellSize
                // Solve: 12*cellSize + 11*gap = available, gap = 0.3*cellSize
                //   → cellSize = available / (12 + 11*0.3) = available / 15.3
                val cellSize = (available / 15.3f).coerceIn(14.dp, 26.dp)
                val monthGap = (cellSize * 0.3f).coerceIn(3.dp, 8.dp)
                val rowSpacing = cellSize * 0.35f

                EntireYearGrid(
                    year = year,
                    moodMap = moodMap,
                    themeType = themeType,
                    emptyColor = emptyColor,
                    labelColor = labelColor,
                    cellSize = cellSize,
                    monthGap = monthGap,
                    rowSpacing = rowSpacing,
                    dayLabelWidth = dayLabelWidth,
                    labelToDotsGap = labelToDotsGap,
                    gridWidth = maxWidth          // stretch to full available width
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EntireYearGrid(
    year: Int,
    moodMap: Map<String, MoodFlowDto>,
    themeType: MoonThemeType,
    emptyColor: Color,
    labelColor: Color,
    cellSize: Dp,
    monthGap: Dp,
    rowSpacing: Dp,
    dayLabelWidth: Dp,
    labelToDotsGap: Dp = 6.dp,
    gridWidth: Dp = Dp.Unspecified
) {
    val widthModifier = if (gridWidth != Dp.Unspecified) Modifier.width(gridWidth) else Modifier

    Column(modifier = widthModifier.offset(x = (-8).dp)) {
        // ── Month header row ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Spacer for day-label column + gap
            Spacer(modifier = Modifier.width(dayLabelWidth + labelToDotsGap))
            (1..12).forEachIndexed { idx, month ->
                if (idx > 0) Spacer(modifier = Modifier.width(monthGap))
                Text(
                    text = "$month",
                    fontSize = 9.sp,
                    color = labelColor,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(cellSize)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ── Rows for days 1..31 ──
        (1..31).forEach { day ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Day label column — right-aligned, only day 1 and multiples of 5
                Box(
                    modifier = Modifier.width(dayLabelWidth),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (day == 1 || day % 5 == 0) {
                        Text(
                            text = "$day",
                            fontSize = 8.sp,
                            color = labelColor,
                            textAlign = TextAlign.End
                        )
                    }
                }

                // Gap between label column and first dot
                Spacer(modifier = Modifier.width(labelToDotsGap))

                // Month dots
                (1..12).forEachIndexed { idx, month ->
                    if (idx > 0) Spacer(modifier = Modifier.width(monthGap))
                    val isValid = try {
                        java.time.LocalDate.of(year, month, day); true
                    } catch (_: Exception) { false }

                    if (isValid) {
                        val dateStr = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month, day)
                        val mood = moodMap[dateStr]
                        val color = if (mood != null)
                            MoonIcons.Moods.getMoodColor(mood.moodId.toInt(), themeType)
                        else emptyColor

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(CircleShape)
                                .background(color)
                        )
                    } else {
                        // Invalid date — invisible placeholder keeps alignment
                        Spacer(modifier = Modifier.size(cellSize))
                    }
                }
            }
            if (day < 31) Spacer(modifier = Modifier.height(rowSpacing))
        }
    }
}

// ────────────────────────────────────────────────────────────────
// By Month Tab
// ────────────────────────────────────────────────────────────────

@Composable
private fun ByMonthContent(
    year: Int,
    moodData: List<MoodFlowDto>,
    themeType: MoonThemeType,
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer
) {
    val moodMap = remember(moodData) { moodData.associateBy { it.date } }
    val emptyColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.13f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Year heading
        Text(
            text = "$year",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
        )

        // Capture wrapper — explicit background for correct light/dark screenshot
        val captureBackground = MaterialTheme.colorScheme.background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(captureBackground)
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val monthPairs = (1..12).chunked(2)
                monthPairs.forEach { pair ->
                    // IntrinsicSize.Max makes both cards in the same row
                    // match the height of the taller card — no fixed height needed,
                    // so dots are never squished or distorted.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(androidx.compose.foundation.layout.IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { month ->
                            MonthBeanCard(
                                year = year,
                                month = month,
                                moodMap = moodMap,
                                themeType = themeType,
                                emptyColor = emptyColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun MonthBeanCard(
    year: Int,
    month: Int,
    moodMap: Map<String, MoodFlowDto>,
    themeType: MoonThemeType,
    emptyColor: Color,
    modifier: Modifier = Modifier
) {
    val monthName = java.time.Month.of(month).name.take(3)
        .lowercase().replaceFirstChar { it.titlecase() }
    val daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

    // Day-of-week offset for the 1st of this month
    // java DayOfWeek: MONDAY=1 … SUNDAY=7 → Mon=0, Tue=1, … Sun=6
    val firstDayOffset = java.time.LocalDate.of(year, month, 1).dayOfWeek.value - 1

    val dotsPerRow = 7   // 7 days = 1 week per row
    val totalRows = 6    // max possible weeks a month can span
    // Larger dots + spacing so cards are taller and dots are clearly circular
    val dotSize = 16.dp
    val dotSpacing = 5.dp

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = surface,
        border = BorderStroke(1.dp, outline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month name
            Text(
                text = monthName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Weekly calendar grid — 7 columns (Mon–Sun), up to 6 rows
            // cellIndex=0 is Mon of week 1; firstDayOffset pushes day 1
            // to the correct day-of-week column.
            Column(
                verticalArrangement = Arrangement.spacedBy(dotSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in 0 until totalRows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(dotSpacing)) {
                        for (col in 0 until dotsPerRow) {
                            val cellIndex = row * dotsPerRow + col
                            val day = cellIndex - firstDayOffset + 1

                            if (day in 1..daysInMonth) {
                                val dateStr = String.format(
                                    Locale.ENGLISH, "%04d-%02d-%02d", year, month, day
                                )
                                val mood = moodMap[dateStr]
                                val dotColor = if (mood != null)
                                    MoonIcons.Moods.getMoodColor(mood.moodId.toInt(), themeType)
                                else emptyColor

                                Box(
                                    modifier = Modifier
                                        .size(dotSize)  // fixed square → always a perfect circle
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            } else {
                                // Empty cell — invisible spacer, keeps grid aligned
                                Spacer(modifier = Modifier.size(dotSize))
                            }
                        }
                    }
                }
            }
        }
    }
}
