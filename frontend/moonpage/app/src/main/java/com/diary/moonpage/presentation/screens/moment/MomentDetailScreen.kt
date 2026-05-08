package com.diary.moonpage.presentation.screens.moment

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is MomentUiEffect.ShareMoment -> {
                    coroutineScope.launch {
                        val request = ImageRequest.Builder(context)
                            .data(effect.url)
                            .build()
                        val result = context.imageLoader.execute(request)
                        if (result is coil.request.SuccessResult) {
                            val bitmap = (result.drawable as android.graphics.drawable.BitmapDrawable).bitmap
                            com.diary.moonpage.core.util.ImageUtils.shareImage(context, bitmap, "Share Moment")
                        }
                    }
                }
                is MomentUiEffect.DownloadMoment -> {
                    com.diary.moonpage.core.util.ImageUtils.downloadAndSaveImage(context, effect.url)
                }
                else -> {}
            }
        }
    }

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
