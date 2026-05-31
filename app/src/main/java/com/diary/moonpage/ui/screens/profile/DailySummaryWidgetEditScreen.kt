package com.diary.moonpage.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.WidgetPreferencesManager
import com.diary.moonpage.ui.screens.profile.components.DailySummaryWidgetPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailySummaryWidgetEditViewModel @Inject constructor(
    private val widgetPreferencesManager: WidgetPreferencesManager
) : ViewModel() {

    val uiState: StateFlow<DailySummaryWidgetEditUiState> = combine(
        widgetPreferencesManager.showDailyStreak,
        widgetPreferencesManager.showDailyNote,
        widgetPreferencesManager.showDailyStats
    ) { streak, note, stats ->
        DailySummaryWidgetEditUiState(
            showStreak = streak,
            showNote = note,
            showStats = stats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailySummaryWidgetEditUiState()
    )

    fun setShowStreak(show: Boolean) {
        viewModelScope.launch {
            widgetPreferencesManager.setShowDailyStreak(show)
        }
    }

    fun setShowNote(show: Boolean) {
        viewModelScope.launch {
            widgetPreferencesManager.setShowDailyNote(show)
        }
    }

    fun setShowStats(show: Boolean) {
        viewModelScope.launch {
            widgetPreferencesManager.setShowDailyStats(show)
        }
    }
}

data class DailySummaryWidgetEditUiState(
    val showStreak: Boolean = true,
    val showNote: Boolean = true,
    val showStats: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryWidgetEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailySummaryWidgetEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.widget_daily_summary_name),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            DailySummaryWidgetPreview(
                showStreak = uiState.showStreak,
                showNote = uiState.showNote,
                showStats = uiState.showStats,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Text(
                text = stringResource(R.string.preferences),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            EditToggleItem(
                title = stringResource(R.string.show_streak_badge),
                description = stringResource(R.string.show_streak_badge_widget_desc),
                icon = Icons.Rounded.Whatshot,
                checked = uiState.showStreak,
                onCheckedChange = { viewModel.setShowStreak(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorScheme.outlineVariant)

            EditToggleItem(
                title = stringResource(R.string.show_daily_note),
                description = stringResource(R.string.show_daily_note_desc),
                icon = Icons.Rounded.Note,
                checked = uiState.showNote,
                onCheckedChange = { viewModel.setShowNote(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorScheme.outlineVariant)

            EditToggleItem(
                title = stringResource(R.string.show_stats_footer),
                description = stringResource(R.string.show_stats_footer_desc),
                icon = Icons.Rounded.BarChart,
                checked = uiState.showStats,
                onCheckedChange = { viewModel.setShowStats(it) }
            )
        }
    }
}

@Composable
fun EditToggleItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
