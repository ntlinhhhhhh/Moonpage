package com.diary.moonpage.presentation.screens.moment

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful Component
 */
@Composable
fun MomentDetailScreen(
    momentId: String,
    viewModel: MomentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MomentDetailScreenContent(
        uiState = uiState,
        momentId = momentId,
        onNavigateBack = onNavigateBack,
        onNavigateToGallery = onNavigateToGallery,
        onShare = { moment -> viewModel.onEvent(MomentUiEvent.ShareMoment(moment.imageUrl)) },
        onDownload = { moment -> viewModel.onEvent(MomentUiEvent.DownloadMoment(moment.imageUrl)) },
        onDelete = { moment -> viewModel.onEvent(MomentUiEvent.DeleteMoment(moment.id)) }
    )
}

/**
 * Stateless Component
 */
@Composable
fun MomentDetailScreenContent(
    uiState: MomentUiState,
    momentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onShare: (com.diary.moonpage.domain.model.Moment) -> Unit,
    onDownload: (com.diary.moonpage.domain.model.Moment) -> Unit,
    onDelete: (com.diary.moonpage.domain.model.Moment) -> Unit
) {
    // Reuse MomentHistoryScreenContent for the detail view logic (scrolling through moments)
    MomentHistoryScreenContent(
        moments = uiState.moments,
        localPaths = uiState.localPaths,
        initialMomentId = momentId,
        onNavigateToGallery = onNavigateToGallery,
        onBackToCamera = onNavigateBack,
        onShare = onShare,
        onDownload = onDownload,
        onDelete = onDelete
    )
}
