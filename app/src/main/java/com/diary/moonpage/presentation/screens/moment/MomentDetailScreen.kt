package com.diary.moonpage.presentation.screens.moment

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import com.diary.moonpage.presentation.components.moment.MomentZoomOverlay
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share

/**
 * Stateful Component
 */
@Composable
fun MomentDetailScreen(
    momentId: String,
    viewModel: MomentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var zoomImage by remember { mutableStateOf<String?>(null) }
    var momentToDelete by remember { mutableStateOf<com.diary.moonpage.domain.model.Moment?>(null) }

    if (momentToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { momentToDelete = null },
            containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.popupBgColor,
            title = { androidx.compose.material3.Text("Delete Moment", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = { androidx.compose.material3.Text("Are you sure you want to delete this moment? This action cannot be undone.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.onEvent(MomentUiEvent.DeleteMoment(momentToDelete!!.id))
                        momentToDelete = null
                        onNavigateBack() // Go back after deleting in detail view
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error)
                ) { androidx.compose.material3.Text("Delete") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { momentToDelete = null },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnTextColor)
                ) { androidx.compose.material3.Text("Cancel") }
            }
        )
    }

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
        onNavigateToAccount = onNavigateToAccount,
        onShare = { moment -> viewModel.onEvent(MomentUiEvent.ShareMoment(moment.imageUrl)) },
        onDownload = { moment -> viewModel.onEvent(MomentUiEvent.DownloadMoment(moment.imageUrl)) },
        onDelete = { moment -> momentToDelete = moment },
        onImageZoom = { url -> zoomImage = url }
    )

    if (zoomImage != null) {
        MomentZoomOverlay(
            imageUrl = zoomImage!!,
            localPath = uiState.localPaths[zoomImage!!],
            onDismiss = { zoomImage = null },
            onShare = {
                viewModel.onEvent(MomentUiEvent.ShareMoment(zoomImage!!))
            }
        )
    }
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
    onNavigateToAccount: () -> Unit,
    onShare: (com.diary.moonpage.domain.model.Moment) -> Unit,
    onDownload: (com.diary.moonpage.domain.model.Moment) -> Unit,
    onDelete: (com.diary.moonpage.domain.model.Moment) -> Unit,
    onImageZoom: (String) -> Unit
) {
    // Reuse MomentHistoryScreenContent for the detail view logic (scrolling through moments)
    MomentHistoryScreenContent(
        moments = uiState.moments,
        localPaths = uiState.localPaths,
        initialMomentId = momentId,
        onNavigateToGallery = onNavigateToGallery,
        onBackToCamera = onNavigateBack,
        onNavigateToAccount = onNavigateToAccount,
        onShare = onShare,
        onDownload = onDownload,
        onDelete = onDelete,
        onImageZoom = onImageZoom
    )
}
