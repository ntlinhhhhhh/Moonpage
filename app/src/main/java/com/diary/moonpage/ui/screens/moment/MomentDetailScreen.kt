package com.diary.moonpage.ui.screens.moment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.diary.moonpage.ui.screens.moment.components.MomentFeedItem
import com.diary.moonpage.ui.screens.moment.components.MomentZoomOverlay
import com.diary.moonpage.domain.model.Moment
import kotlinx.coroutines.launch

/**
 * Stateful Component
 */
@Composable
fun MomentDetailRoute(
    momentId: String,
    viewModel: MomentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var zoomMoment by remember { mutableStateOf<Moment?>(null) }
    var momentToDelete by remember { mutableStateOf<Moment?>(null) }

    if (momentToDelete != null) {
        AlertDialog(
            onDismissRequest = { momentToDelete = null },
            containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.popupBgColor,
            title = { Text("Delete Moment", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this moment? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(MomentUiEvent.DeleteMoment(momentToDelete!!.id))
                        momentToDelete = null
                        onNavigateBack() 
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = { momentToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnTextColor)
                ) { Text("Cancel") }
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

    MomentDetailScreen(
        uiState = uiState,
        momentId = momentId,
        onNavigateBack = onNavigateBack,
        onNavigateToGallery = onNavigateToGallery,
        onNavigateToAccount = onNavigateToAccount,
        onShare = { m: Moment -> viewModel.onEvent(MomentUiEvent.ShareMoment(m.imageUrl)) },
        onDownload = { m: Moment -> viewModel.onEvent(MomentUiEvent.DownloadMoment(m.imageUrl)) },
        onDelete = { m: Moment -> momentToDelete = m },
        onImageZoom = { moment: Moment -> zoomMoment = moment }
    )

    if (zoomMoment != null) {
        MomentZoomOverlay(
            imageUrl = zoomMoment!!.imageUrl,
            localPath = resolveMomentLocalPath(zoomMoment!!, uiState.localPaths),
            onDismiss = { zoomMoment = null },
            onShare = {
                viewModel.onEvent(MomentUiEvent.ShareMoment(zoomMoment!!.imageUrl))
            }
        )
    }
}

/**
 * Stateless Component
 */
@Composable
fun MomentDetailScreen(
    uiState: MomentUiState,
    momentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onShare: (Moment) -> Unit,
    onDownload: (Moment) -> Unit,
    onDelete: (Moment) -> Unit,
    onImageZoom: (Moment) -> Unit
) {
    val moment = uiState.moments.find { it.id == momentId }

    if (moment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Moment not found")
            IconButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
                Icon(Icons.Rounded.Close, "Back")
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)) {
        MomentFeedItem(
            moment = moment,
            localPath = resolveMomentLocalPath(moment, uiState.localPaths),
            onImageClick = { onImageZoom(moment) }
        )
        
        // Overlay controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Rounded.Close, "Close", tint = androidx.compose.ui.graphics.Color.White)
            }
            
            Row {
                IconButton(onClick = { onShare(moment) }) {
                    Icon(Icons.Rounded.Share, "Share", tint = androidx.compose.ui.graphics.Color.White)
                }
                IconButton(onClick = { onDelete(moment) }) {
                    Icon(Icons.Rounded.Delete, "Delete", tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}
