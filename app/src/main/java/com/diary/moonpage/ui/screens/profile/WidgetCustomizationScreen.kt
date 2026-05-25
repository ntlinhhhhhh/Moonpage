package com.diary.moonpage.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diary.moonpage.R

data class WidgetInfo(
    val titleRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetCustomizationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDailySummaryEdit: () -> Unit,
    onNavigateToPhotoMomentEdit: () -> Unit,
    onNavigateToQuickMoodEdit: () -> Unit,
    onNavigateToWeeklyMoodEdit: () -> Unit,
    onNavigateToMonthlyMoodEdit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    val availableWidgets = listOf(
        WidgetInfo(
            titleRes = R.string.widget_daily_summary_name,
            descriptionRes = R.string.widget_daily_summary_description,
            icon = Icons.Rounded.Dashboard,
            onClick = onNavigateToDailySummaryEdit
        ),
        WidgetInfo(
            titleRes = R.string.widget_photo_moment_name,
            descriptionRes = R.string.widget_photo_moment_description,
            icon = Icons.Rounded.Photo,
            onClick = onNavigateToPhotoMomentEdit
        ),
        WidgetInfo(
            titleRes = R.string.widget_quick_mood_label,
            descriptionRes = R.string.widget_quick_mood_desc,
            icon = Icons.Rounded.Mood,
            onClick = onNavigateToQuickMoodEdit
        ),
        WidgetInfo(
            titleRes = R.string.widget_weekly_mood_label,
            descriptionRes = R.string.widget_weekly_mood_desc,
            icon = Icons.Rounded.DateRange,
            onClick = onNavigateToWeeklyMoodEdit
        ),
        WidgetInfo(
            titleRes = R.string.widget_monthly_mood_label,
            descriptionRes = R.string.widget_monthly_mood_desc,
            icon = Icons.Rounded.CalendarMonth,
            onClick = onNavigateToMonthlyMoodEdit
        )
    )

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.widgets),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.more),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(availableWidgets) { widget ->
                WidgetCard(widget = widget)
            }
        }
    }
}

@Composable
fun WidgetCard(widget: WidgetInfo) {
    ElevatedCard(
        onClick = widget.onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = widget.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(widget.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(widget.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
