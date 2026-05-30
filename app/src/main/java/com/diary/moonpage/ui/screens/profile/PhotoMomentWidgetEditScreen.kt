package com.diary.moonpage.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.diary.moonpage.ui.screens.profile.components.PhotoMomentWidgetPreview
import com.diary.moonpage.widget.glance.MoonpageWidgets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoMomentWidgetEditViewModel @Inject constructor(
    private val widgetPreferencesManager: WidgetPreferencesManager
) : ViewModel() {

    val uiState: StateFlow<PhotoMomentWidgetEditUiState> = combine(
        widgetPreferencesManager.showPhotoStreak,
        widgetPreferencesManager.photoDisplayMode
    ) { streak, mode ->
        PhotoMomentWidgetEditUiState(
            showStreak = streak,
            photoDisplayMode = mode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PhotoMomentWidgetEditUiState()
    )

    fun setShowStreak(show: Boolean) {
        viewModelScope.launch {
            widgetPreferencesManager.setShowPhotoStreak(show)
        }
    }

    fun setPhotoDisplayMode(mode: String) {
        viewModelScope.launch {
            widgetPreferencesManager.setPhotoDisplayMode(mode)
        }
    }
}

data class PhotoMomentWidgetEditUiState(
    val showStreak: Boolean = true,
    val photoDisplayMode: String = "CROP"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoMomentWidgetEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: PhotoMomentWidgetEditViewModel = hiltViewModel()
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
                        stringResource(R.string.widget_photo_moment_name),
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
            PhotoMomentWidgetPreview(
                showStreak = uiState.showStreak,
                displayMode = uiState.photoDisplayMode,
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
                description = stringResource(R.string.show_streak_badge_photo_desc),
                icon = Icons.Rounded.Whatshot,
                checked = uiState.showStreak,
                onCheckedChange = { viewModel.setShowStreak(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.primary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.image_display_mode),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.image_display_mode_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DisplayModeCard(
                    title = stringResource(R.string.crop_to_fill),
                    selected = uiState.photoDisplayMode == "CROP",
                    onClick = { viewModel.setPhotoDisplayMode("CROP") },
                    modifier = Modifier.weight(1f)
                )
                DisplayModeCard(
                    title = stringResource(R.string.fit_to_widget),
                    selected = uiState.photoDisplayMode == "FIT",
                    onClick = { viewModel.setPhotoDisplayMode("FIT") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayModeCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
        },
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) colorScheme.primaryContainer.copy(alpha = 0.3f) else colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) colorScheme.primary else colorScheme.onSurface
            )
        }
    }
}
