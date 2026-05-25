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
import com.diary.moonpage.ui.screens.profile.components.MonthlyMoodWidgetPreview
import com.diary.moonpage.ui.screens.profile.components.SwitchSettingItem
import com.diary.moonpage.widget.glance.MoonpageWidgets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonthlyMoodWidgetEditViewModel @Inject constructor(
    private val widgetPreferencesManager: WidgetPreferencesManager
) : ViewModel() {

    val uiState: StateFlow<MonthlyMoodWidgetEditUiState> = widgetPreferencesManager.showMonthlyMoodGrid
        .map { MonthlyMoodWidgetEditUiState(showGrid = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonthlyMoodWidgetEditUiState()
        )

    fun setShowGrid(show: Boolean) {
        viewModelScope.launch {
            widgetPreferencesManager.setShowMonthlyMoodGrid(show)
        }
    }
}

data class MonthlyMoodWidgetEditUiState(
    val showGrid: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyMoodWidgetEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: MonthlyMoodWidgetEditViewModel = hiltViewModel()
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
                        stringResource(R.string.widget_monthly_mood_label),
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
            MonthlyMoodWidgetPreview(showGrid = uiState.showGrid)
            Spacer(modifier = Modifier.height(20.dp))
            SwitchSettingItem(
                title = "Show grid",
                icon = Icons.Rounded.CalendarMonth,
                checked = uiState.showGrid,
                onCheckedChange = { viewModel.setShowGrid(it) }
            )
        }
    }
}
