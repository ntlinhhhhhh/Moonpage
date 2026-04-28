package com.diary.moonpage.presentation.screens.moment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.diary.moonpage.presentation.theme.MoonPageTheme
import com.diary.moonpage.domain.model.Moment
import com.diary.moonpage.presentation.components.moment.MomentFeedItem
import com.diary.moonpage.presentation.components.moment.CaptureButton

@Composable
fun MomentHistoryRoute(
    onBackToCamera: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: MomentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MomentHistoryScreen(
        moments = uiState.moments,
        localPaths = uiState.localPaths,
        onNavigateToGallery = onNavigateToGallery,
        onBackToCamera = onBackToCamera,
        onShare = { moment -> viewModel.onEvent(MomentUiEvent.ShareMoment(moment.imageUrl)) },
        onDownload = { moment -> viewModel.onEvent(MomentUiEvent.DownloadMoment(moment.imageUrl)) },
        onDelete = { moment -> viewModel.onEvent(MomentUiEvent.DeleteMoment(moment.id)) }
    )
}

@Composable
fun MomentHistoryScreen(
    moments: List<Moment>,
    localPaths: Map<String, String>,
    onNavigateToGallery: () -> Unit,
    onBackToCamera: () -> Unit,
    initialMomentId: String? = null,
    onShare: (Moment) -> Unit = {},
    onDownload: (Moment) -> Unit = {},
    onDelete: (Moment) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sortedMoments = remember(moments) { moments.sortedByDescending { it.capturedAt } }
    val initialPage = remember(initialMomentId, sortedMoments) {
        if (initialMomentId != null) {
            sortedMoments.indexOfFirst { it.id == initialMomentId }.coerceAtLeast(0)
        } else 0
    }
    val feedPagerState = rememberPagerState(initialPage = initialPage, pageCount = { sortedMoments.size })
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val bgColor = MaterialTheme.colorScheme.background
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    // Thông minh hơn trong việc pre-fetch: Load ảnh trước và sau trang hiện tại
    LaunchedEffect(feedPagerState.currentPage, sortedMoments) {
        if (sortedMoments.isNotEmpty()) {
            val currentPage = feedPagerState.currentPage
            // Load trang hiện tại, 2 trang trước và 2 trang sau
            val range = (currentPage - 2)..(currentPage + 2)
            range.forEach { index ->
                if (index in sortedMoments.indices) {
                    val moment = sortedMoments[index]
                    val localPath = localPaths[moment.imageUrl]
                    val imageData = if (localPath != null && java.io.File(localPath).exists()) java.io.File(localPath) else moment.imageUrl
                    
                    val request = ImageRequest.Builder(context)
                        .data(imageData)
                        .memoryCacheKey("feed_${imageData}")
                        .diskCachePolicy(if (imageData is java.io.File) CachePolicy.DISABLED else CachePolicy.ENABLED)
                        .build()
                    context.imageLoader.enqueue(request)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        if (sortedMoments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No moments yet", color = onBgColor.copy(alpha = 0.6f))
            }
        } else {
            VerticalPager(
                state = feedPagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 2 // Giữ nhiều trang hơn trong bộ nhớ để scroll mượt
            ) { index ->
                val moment = sortedMoments[index]
                MomentFeedItem(
                    moment = moment, 
                    localPath = localPaths[moment.imageUrl]
                )
            }
        }

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(onBgColor.copy(alpha = 0.15f))
                    .align(Alignment.CenterStart)
            )
        }

        // Bottom Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 40.dp, vertical = 40.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(onBgColor.copy(alpha = 0.1f))
                    .clickable { onNavigateToGallery() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.GridView, null, tint = onBgColor, modifier = Modifier.size(28.dp))
            }

            CaptureButton(onClick = onBackToCamera)

            if (sortedMoments.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(onBgColor.copy(alpha = 0.1f))
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.MoreHoriz, null, tint = onBgColor, modifier = Modifier.size(28.dp))
                }
            } else {
                Spacer(modifier = Modifier.size(52.dp))
            }
        }

        if (showMenu) {
            @OptIn(ExperimentalMaterial3Api::class)
            ModalBottomSheet(
                onDismissRequest = { showMenu = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                showMenu = false
                                if (sortedMoments.isNotEmpty()) {
                                    onShare(sortedMoments[feedPagerState.currentPage])
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Share, null, tint = onBgColor)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Share", color = onBgColor, fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                showMenu = false
                                if (sortedMoments.isNotEmpty()) {
                                    onDownload(sortedMoments[feedPagerState.currentPage])
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Download, null, tint = onBgColor)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Download", color = onBgColor, fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                showMenu = false
                                if (sortedMoments.isNotEmpty()) {
                                    onDelete(sortedMoments[feedPagerState.currentPage])
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMenu = false }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Close, null, tint = onBgColor)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Cancel", color = onBgColor, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "History With Content")
@Composable
fun MomentHistoryScreenPreview() {
    val sampleMoments = listOf(
        Moment(
            id = "1",
            imageUrl = "https://picsum.photos/1000",
            caption = "Chuyến đi Đà Lạt tuyệt vời! 🌲✨",
            capturedAt = "2024-04-26T10:00:00.000Z",
            isPublic = true
        )
    )

    MoonPageTheme {
        MomentHistoryScreen(
            moments = sampleMoments,
            localPaths = emptyMap(),
            onNavigateToGallery = {},
            onBackToCamera = {}
        )
    }
}
