package com.diary.moonpage.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.WidgetPreferencesManager
import com.diary.moonpage.ui.screens.profile.components.QuickMoodWidgetPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickMoodWidgetEditViewModel @Inject constructor(
    private val widgetPreferencesManager: WidgetPreferencesManager
) : ViewModel() {

    val uiState: StateFlow<QuickMoodWidgetEditUiState> = combine(
        widgetPreferencesManager.showQuickMoodStreak,
        widgetPreferencesManager.showQuickMoodLabels
    ) { streak, labels ->
        QuickMoodWidgetEditUiState(showStreak = streak, showLabels = labels)
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QuickMoodWidgetEditUiState()
        )

    fun setShowStreak(show: Boolean) {
        viewModelScope.launch {
            widgetPreferencesManager.setShowQuickMoodStreak(show)
        }
    }

    fun setShowLabels(show: Boolean) {
        viewModelScope.launch {
            widgetPreferencesManager.setShowQuickMoodLabels(show)
        }
    }
}

data class QuickMoodWidgetEditUiState(
    val showStreak: Boolean = true,
    val showLabels: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickMoodWidgetEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuickMoodWidgetEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.widget_quick_mood_label),
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
            QuickMoodWidgetPreview(
                showStreak = uiState.showStreak,
                showLabels = uiState.showLabels,
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
                title = stringResource(R.string.pref_show_streak_badge),
                description = stringResource(R.string.pref_show_streak_badge_desc),
                icon = Icons.Rounded.Whatshot,
                checked = uiState.showStreak,
                onCheckedChange = { viewModel.setShowStreak(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorScheme.outlineVariant)

            EditToggleItem(
                title = stringResource(R.string.pref_show_labels),
                description = stringResource(R.string.pref_show_labels_desc),
                icon = Icons.Rounded.Label,
                checked = uiState.showLabels,
                onCheckedChange = { viewModel.setShowLabels(it) }
            )
        }
    }
}
