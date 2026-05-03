package com.diary.moonpage.presentation.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import com.diary.moonpage.presentation.screens.moment.MomentViewModel
import com.diary.moonpage.domain.model.Moment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMomentDetail: (String) -> Unit = {},
    viewModel: MomentViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    val moments = uiState.moments
    val isLoading = uiState.isLoading

    // Sort moments by capturedAt descending (newest first)
    val sortedMoments = remember(moments) {
        moments.sortedByDescending { it.capturedAt }
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Photo Gallery",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (sortedMoments.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = null,
                            tint = colorScheme.onBackground.copy(alpha = 0.25f),
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            "No photos yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onBackground.copy(alpha = 0.55f)
                        )
                        Text(
                            "Capture your first moment to see it here",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onBackground.copy(alpha = 0.35f)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = sortedMoments,
                        key = { it.id }
                    ) { moment ->
                        GalleryItem(
                            url = moment.imageUrl,
                            localPath = uiState.localPaths[moment.imageUrl],
                            onClick = { onNavigateToMomentDetail(moment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryItem(
    url: String,
    localPath: String?,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // remember to avoid File.exists() running on every recomposition
    val imageData = remember(localPath, url) {
        if (localPath != null && java.io.File(localPath).exists()) java.io.File(localPath) else url
    }

    val isLocalFile = imageData is java.io.File

    val imageRequest = remember(imageData) {
        ImageRequest.Builder(context)
            .data(imageData)
            .size(Size(600, 600))
            .scale(Scale.FILL)
            .crossfade(200)
            .memoryCacheKey("feed_${imageData}")
            .diskCachePolicy(if (isLocalFile) CachePolicy.DISABLED else CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    var isLoaded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val shimmerAlpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isLoaded) Color.Transparent
                else if (isError) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onSuccess = { isLoaded = true },
            onError = { isError = true }
        )
        if (isError) {
            Icon(
                imageVector = Icons.Rounded.BrokenImage,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}
