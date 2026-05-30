package com.diary.moonpage.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.WidgetPreferencesManager
import com.diary.moonpage.ui.screens.profile.components.WeeklyMoodWidgetPreview
import com.diary.moonpage.widget.glance.MoonpageWidgets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeeklyMoodWidgetEditViewModel @Inject constructor(
    private val widgetPreferencesManager: WidgetPreferencesManager
) : ViewModel() {

    val uiState: StateFlow<WeeklyMoodWidgetEditUiState> = combine(
        widgetPreferencesManager.showWeeklyMoodStreak,
        widgetPreferencesManager.showWeeklyMoodDates
    ) { streak, dates ->
        WeeklyMoodWidgetEditUiState(showStreak = streak, showDates = dates)
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyMoodWidgetEditUiState()
        )

    fun setShowStreak(show: Boolean) {
        viewModelScope.launch {
            widgetPreferencesManager.setShowWeeklyMoodStreak(show)
        }
    }

    fun setShowDates(show: Boolean) {
        viewModelScope.launch {
            widgetPreferencesManager.setShowWeeklyMoodDates(show)
        }
    }
}

data class WeeklyMoodWidgetEditUiState(
    val showStreak: Boolean = true,
    val showDates: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyMoodWidgetEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeeklyMoodWidgetEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState) {
        MoonpageWidgets.refreshAll(context)
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.widget_weekly_mood_label),
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
            WeeklyMoodWidgetPreview(
                showStreak = uiState.showStreak,
                showDates = uiState.showDates,
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
                title = "Show streak badge",
                description = "Show your current streak on the widget.",
                icon = Icons.Rounded.Whatshot,
                checked = uiState.showStreak,
                onCheckedChange = { viewModel.setShowStreak(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorScheme.outlineVariant)

            EditToggleItem(
                title = "Show dates",
                description = "Show date numbers under each day of the week.",
                icon = Icons.Rounded.DateRange,
                checked = uiState.showDates,
                onCheckedChange = { viewModel.setShowDates(it) }
            )
        }
    }
}
