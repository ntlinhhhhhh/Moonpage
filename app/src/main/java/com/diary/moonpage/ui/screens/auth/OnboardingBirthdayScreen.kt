package com.diary.moonpage.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth

private val MONTHS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)
private val YEARS = (1950..java.time.LocalDate.now().year).map { it.toString() }

// Large multiplier for infinite circular effect
private const val INFINITE_MULTIPLIER = 1000

/**
 * Stateful Component
 */
@Composable
fun OnboardingBirthdayRoute(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNext: () -> Unit
) {
    // Default: 15th April 2000
    var selectedMonthIndex by remember { mutableIntStateOf(3) }   // April (0-based)
    var selectedDayIndex   by remember { mutableIntStateOf(14) }  // 15 (0-based)
    var selectedYearIndex  by remember { mutableIntStateOf(50) }  // 2000 (index in YEARS)

    // Auto-clamp days when month/year changes
    val daysInMonth by remember {
        derivedStateOf {
            val year = YEARS.getOrElse(selectedYearIndex) { "2000" }.toIntOrNull() ?: 2000
            val month = selectedMonthIndex + 1
            YearMonth.of(year, month).lengthOfMonth()
        }
    }
    // If current day exceeds days in selected month, clamp it
    LaunchedEffect(daysInMonth) {
        if (selectedDayIndex >= daysInMonth) {
            selectedDayIndex = daysInMonth - 1
        }
    }

    OnboardingBirthdayScreen(
        selectedMonthIndex = selectedMonthIndex,
        selectedDayIndex = selectedDayIndex,
        selectedYearIndex = selectedYearIndex,
        daysInMonth = daysInMonth,
        onMonthChange = { selectedMonthIndex = it },
        onDayChange = { selectedDayIndex = it },
        onYearChange = { selectedYearIndex = it },
        onNavigateBack = onNavigateBack,
        onNext = {
            val birthDate = "${YEARS[selectedYearIndex]}-${String.format("%02d", selectedMonthIndex + 1)}-${String.format("%02d", selectedDayIndex + 1)}"
            viewModel.setBirthday(birthDate)
            onNext()
        }
    )
}

/**
 * Stateless Component
 */
@Composable
fun OnboardingBirthdayScreen(
    selectedMonthIndex: Int,
    selectedDayIndex: Int,
    selectedYearIndex: Int,
    daysInMonth: Int,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onNext: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current

    val progressAnim by animateFloatAsState(
        targetValue = 0.5f,
        animationSpec = tween(600),
        label = "progress"
    )

    Scaffold(containerColor = colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = colorScheme.onBackground
                    )
                }
                LinearProgressIndicator(
                    progress = { progressAnim },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .padding(horizontal = 8.dp),
                    color = colorScheme.primary,
                    trackColor = colorScheme.primary.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Header ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Happy Birthday! 🎂",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "When did you enter this world?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Date Picker UI ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Highlight Row
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 16.dp),
                    color = colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp)
                ) {}

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Month
                    Box(modifier = Modifier.weight(1f)) {
                        InfiniteCircularList(
                            items = MONTHS,
                            initialIndex = selectedMonthIndex,
                            onIndexChange = { onMonthChange(it) }
                        )
                    }

                    // Day
                    Box(modifier = Modifier.weight(0.7f)) {
                        InfiniteCircularList(
                            items = (1..daysInMonth).map { it.toString() },
                            initialIndex = selectedDayIndex,
                            onIndexChange = { onDayChange(it) }
                        )
                    }

                    // Year
                    Box(modifier = Modifier.weight(1.2f)) {
                        InfiniteCircularList(
                            items = YEARS,
                            initialIndex = selectedYearIndex,
                            onIndexChange = { onYearChange(it) },
                            isInfinite = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.2f))

            // ── Next Button ──────────────────────────────────────────────────
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfiniteCircularList(
    items: List<String>,
    initialIndex: Int,
    onIndexChange: (Int) -> Unit,
    isInfinite: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val itemHeight = 60.dp
    val visibleItems = 5
    
    val totalItems = if (isInfinite) items.size * INFINITE_MULTIPLIER else items.size
    val firstVisibleIndex = if (isInfinite) (items.size * INFINITE_MULTIPLIER / 2) + initialIndex else initialIndex
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Sync selected index based on scroll
    val currentIndex by remember {
        derivedStateOf {
            val centerIndex = listState.firstVisibleItemIndex + (visibleItems / 2)
            centerIndex % items.size
        }
    }

    LaunchedEffect(currentIndex) {
        onIndexChange(currentIndex)
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Box(modifier = Modifier.height(itemHeight * visibleItems)) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(totalItems) { index ->
                val actualIndex = index % items.size
                val isSelected = actualIndex == currentIndex
                
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[actualIndex],
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = if (isSelected) 22.sp else 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
