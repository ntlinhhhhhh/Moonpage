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
import com.diary.moonpage.ui.screens.profile.components.SwitchSettingItem
import com.diary.moonpage.ui.screens.profile.components.WeeklyMoodWidgetPreview
import com.diary.moonpage.widget.glance.MoonpageWidgets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeeklyMoodWidgetEditViewModel @Inject constructor(
    private val widgetPreferencesManager: WidgetPreferencesManager
) : ViewModel() {

    val uiState: StateFlow<WeeklyMoodWidgetEditUiState> = widgetPreferencesManager.showWeeklyMoodDates
        .map { WeeklyMoodWidgetEditUiState(showDates = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyMoodWidgetEditUiState()
        )

    fun setShowDates(show: Boolean) {
        viewModelScope.launch {
            widgetPreferencesManager.setShowWeeklyMoodDates(show)
        }
    }
}

data class WeeklyMoodWidgetEditUiState(
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
            WeeklyMoodWidgetPreview()
            Spacer(modifier = Modifier.height(20.dp))
            SwitchSettingItem(
                title = "Show dates",
                icon = Icons.Rounded.DateRange,
                checked = uiState.showDates,
                onCheckedChange = { viewModel.setShowDates(it) }
            )
        }
    }
}
