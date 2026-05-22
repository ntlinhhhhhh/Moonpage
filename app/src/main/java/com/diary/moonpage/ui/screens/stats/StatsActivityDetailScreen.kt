package com.diary.moonpage.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonTheme
import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.ui.screens.stats.components.MoonActivityIcon
import com.diary.moonpage.core.util.MoonIcons

enum class FrequentlySortOrder { MOST, LEAST }

@Composable
fun StatsActivityDetailRoute(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsActivityDetailScreen(
        activities = uiState.frequentlyRecorded,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsActivityDetailScreen(
    activities: List<BestActivityDto>,
    onNavigateBack: () -> Unit
) {
    var sortOrder by remember { mutableStateOf(FrequentlySortOrder.MOST) }
    var showSortSheet by remember { mutableStateOf(false) }

    val sortedActivities = remember(activities, sortOrder) {
        when (sortOrder) {
            FrequentlySortOrder.MOST -> activities.sortedByDescending { it.occurrence }
            FrequentlySortOrder.LEAST -> activities.sortedBy { it.occurrence }
        }
    }

    val sortLabel = when (sortOrder) {
        FrequentlySortOrder.MOST -> stringResource(R.string.sort_most_records)
        FrequentlySortOrder.LEAST -> stringResource(R.string.sort_least_records)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.stats_activities_habits),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { /* help */ }) {
                        Icon(Icons.Rounded.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All" filter chip
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = {
                        Text("All", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    },
                    trailingIcon = {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = true,
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent
                    )
                )

                // Sort chip
                FilterChip(
                    selected = true,
                    onClick = { showSortSheet = true },
                    label = {
                        Text(sortLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    },
                    trailingIcon = {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = true,
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent
                    )
                )
            }

            // Activities ranked list
            if (sortedActivities.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No activity data available.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    itemsIndexed(sortedActivities) { index, activity ->
                        FrequentlyRecordedListItem(
                            rank = index + 1,
                            activity = activity,
                            occurrenceChange = activity.occurrence
                        )
                        if (index < sortedActivities.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }

    // Sort bottom sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.sort_by),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { showSortSheet = false }) {
                        Icon(Icons.Rounded.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // Most option
                SortOptionItem(
                    label = stringResource(R.string.sort_most_records),
                    isSelected = sortOrder == FrequentlySortOrder.MOST,
                    onClick = {
                        sortOrder = FrequentlySortOrder.MOST
                        showSortSheet = false
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Least option
                SortOptionItem(
                    label = stringResource(R.string.sort_least_records),
                    isSelected = sortOrder == FrequentlySortOrder.LEAST,
                    onClick = {
                        sortOrder = FrequentlySortOrder.LEAST
                        showSortSheet = false
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun FrequentlyRecordedListItem(
    rank: Int,
    activity: BestActivityDto,
    occurrenceChange: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val icon = MoonIcons.getIconForActivity(activity.activityName)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Rank
        Text(
            text = "$rank",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.width(24.dp)
        )

        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MoonTheme.customColors.logItemBg.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            MoonActivityIcon(icon = icon, size = 26.dp)
        }

        // Name & count
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.activityName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = onSurface
            )
            Text(
                text = "x${activity.occurrence}",
                fontSize = 12.sp,
                color = onSurfaceVariant.copy(alpha = 0.55f)
            )
        }

        // Change indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowDropUp,
                contentDescription = null,
                tint = Color(0xFFE07B39),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "$occurrenceChange",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE07B39)
            )
        }
    }
}

@Composable
private fun SortOptionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) Modifier.background(primary.copy(alpha = 0.06f))
                else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) primary else onSurface
        )
    }
}
