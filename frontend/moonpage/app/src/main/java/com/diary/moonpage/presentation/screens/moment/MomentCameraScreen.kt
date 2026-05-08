package com.diary.moonpage.presentation.screens.moment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.diary.moonpage.presentation.components.moment.CameraMainUI
import com.diary.moonpage.presentation.components.moment.MomentTag
import com.diary.moonpage.presentation.components.moment.TagChip
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Stateful Component
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MomentCameraScreen(
    onNavigateToGallery: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: MomentViewModel = hiltViewModel(),
    profileViewModel: com.diary.moonpage.presentation.screens.profile.ProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val cameraPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(Unit) {
        cameraPermissionState.launchMultiplePermissionRequest()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                MomentUiEffect.UploadSuccess -> {
                    isSuccess = true
                }
                is MomentUiEffect.ShowSnackBar -> {
                    Toast.makeText(context, effect.message.asString(context), Toast.LENGTH_SHORT).show()
                }
                is MomentUiEffect.ShareMoment -> {
                    coroutineScope.launch {
                        val request = ImageRequest.Builder(context)
                            .data(effect.url)
                            .build()
                        val result = context.imageLoader.execute(request)
                        if (result is coil.request.SuccessResult) {
                            val bitmap = (result.drawable as android.graphics.drawable.BitmapDrawable).bitmap
                            com.diary.moonpage.core.util.ImageUtils.shareImage(context, bitmap, "Share Moment")
                        } else {
                            Toast.makeText(context, "Failed to load image for sharing", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is MomentUiEffect.DownloadMoment -> {
                    coroutineScope.launch {
                        com.diary.moonpage.core.util.ImageUtils.downloadAndSaveImage(context, effect.url)
                    }
                }
                is MomentUiEffect.NavigateToDetail -> {
                    // Handled by navigation
                }
            }
        }
    }

    val cameraPermission = cameraPermissionState.permissions.find { it.permission == Manifest.permission.CAMERA }
    if (cameraPermission?.status?.isGranted == true) {
        MomentCameraScreenContent(
            uiState = uiState,
            allTags = viewModel.allTags,
            locationPermissionState = locationPermissionState,
            onEvent = viewModel::onEvent,
            onNavigateToGallery = onNavigateToGallery,
            onNavigateToHistory = onNavigateToHistory,
            avatarUrl = profileState.user?.avatarUrl,
            localAvatarPath = profileState.localAvatarPath ?: profileState.tempAvatarPath,
            isSuccess = isSuccess,
            onResetSuccess = { isSuccess = false }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

/**
 * Stateless Component
 */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MomentCameraScreenContent(
    uiState: MomentUiState,
    allTags: List<MomentTag>,
    locationPermissionState: com.google.accompanist.permissions.MultiplePermissionsState,
    onEvent: (MomentUiEvent) -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToHistory: () -> Unit,
    avatarUrl: String? = null,
    localAvatarPath: String? = null,
    isSuccess: Boolean = false,
    onResetSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedLensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    
    val verticalPagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedImageUri = uri
            capturedLensFacing = CameraSelector.LENS_FACING_BACK
        }
    }

    var userMessage by remember { mutableStateOf("") }
    var userRating by remember { mutableFloatStateOf(0.0f) }
    var userLocation by remember { mutableStateOf("") }
    var userWeather by remember { mutableStateOf("Sunny ☀️") }
    var showTagSheet by remember { mutableStateOf(false) }

    val uploadPagerState = rememberPagerState(pageCount = { allTags.size })

    val weatherIcons = listOf("Sunny ☀️", "Cloudy ☁️", "Rainy 🌧️", "Snowy ❄️", "Windy 💨")

    val reverseGeocode = { location: Location ->
        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val district = address.subLocality ?: address.locality ?: address.subAdminArea
                    val city = address.adminArea ?: address.locality ?: "Unknown"
                    val locationName = if (district != null && district != city) "$district/$city" else city
                    withContext(Dispatchers.Main) { userLocation = locationName }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { userLocation = "Location error" }
            }
        }
    }

    fun isGpsEnabled(ctx: android.content.Context): Boolean {
        val lm = ctx.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    var showGpsDialog by remember { mutableStateOf(false) }

    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            title = { Text("Location Services Off") },
            text = { Text("Please enable Location Services to add your location.") },
            confirmButton = {
                TextButton(onClick = {
                    showGpsDialog = false
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) { Text("Open Settings") }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDialog = false }) { Text("Cancel") }
            }
        )
    }

    val fetchLocationFast = {
        if (locationPermissionState.allPermissionsGranted) {
            if (!isGpsEnabled(context)) {
                showGpsDialog = true
            } else if (userLocation.isEmpty() || userLocation == "Location error") {
                userLocation = "Locating..."
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) reverseGeocode(loc)
                    else {
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                            .addOnSuccessListener { freshLoc -> if (freshLoc != null) reverseGeocode(freshLoc) else userLocation = "" }
                            .addOnFailureListener { userLocation = "" }
                    }
                }
            }
        } else {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    var pendingLocationRequest by remember { mutableStateOf(false) }

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted && pendingLocationRequest) {
            pendingLocationRequest = false
            if (!isGpsEnabled(context)) showGpsDialog = true
            else fetchLocationFast()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (capturedImageUri == null) {
            VerticalPager(
                state = verticalPagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { page ->
                if (page == 0) {
                    CameraMainUI(
                        onSelectFromGallery = { galleryLauncher.launch("image/*") },
                        onNavigateToHistory = { scope.launch { verticalPagerState.animateScrollToPage(1) } },
                        onImageCaptured = { uri, lensFacing ->
                            capturedImageUri = uri
                            capturedLensFacing = lensFacing
                        },
                        avatarUrl = avatarUrl
                    )
                } else {
                    MomentHistoryScreenContent(
                        moments = uiState.moments,
                        localPaths = uiState.localPaths,
                        onNavigateToGallery = onNavigateToGallery,
                        onBackToCamera = { scope.launch { verticalPagerState.animateScrollToPage(0) } },
                        onShare = { onEvent(MomentUiEvent.ShareMoment(it.imageUrl)) },
                        onDownload = { onEvent(MomentUiEvent.DownloadMoment(it.imageUrl)) },
                        onDelete = { onEvent(MomentUiEvent.DeleteMoment(it.id)) },
                        avatarUrl = avatarUrl,
                        localAvatarPath = localAvatarPath
                    )
                }
            }
        } else {
            MomentUploadScreen(
                capturedImageUri = capturedImageUri!!,
                capturedLensFacing = capturedLensFacing,
                pagerState = uploadPagerState,
                allTags = allTags,
                userMessage = userMessage,
                onUserMessageChange = { userMessage = it },
                userRating = userRating,
                onUserRatingChange = { userRating = it },
                userLocation = userLocation,
                onLocationClick = {
                    if (!locationPermissionState.allPermissionsGranted) {
                        pendingLocationRequest = true
                        locationPermissionState.launchMultiplePermissionRequest()
                    } else if (!isGpsEnabled(context)) {
                        showGpsDialog = true
                    } else {
                        fetchLocationFast()
                    }
                },
                userWeather = userWeather,
                onWeatherClick = {
                    val currentIndex = weatherIcons.indexOf(userWeather)
                    userWeather = weatherIcons[(currentIndex + 1) % weatherIcons.size]
                },
                isLoading = uiState.isUploading,
                isSuccess = isSuccess,
                onCancel = { capturedImageUri = null },
                onUpload = { file, caption ->
                    val currentTag = allTags[uploadPagerState.currentPage]
                    onEvent(MomentUiEvent.UploadMoment(
                        imageFile = file,
                        caption = caption,
                        location = if (currentTag.id == "location") userLocation else null,
                        weather = if (currentTag.id == "weather") userWeather else null,
                        rating = if (currentTag.id == "review") userRating else null
                    ))
                },
                onShowTagSheet = { showTagSheet = true }
            )

            if (isSuccess) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(500)
                    capturedImageUri = null
                    onResetSuccess()
                    userMessage = ""
                    userRating = 0.0f
                    userLocation = ""
                }
            }
        }

        if (showTagSheet) {
            ModalBottomSheet(onDismissRequest = { showTagSheet = false }) {
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    allTags.forEachIndexed { index, tag ->
                        TagChip(tag) {
                            scope.launch { uploadPagerState.animateScrollToPage(index) }
                            showTagSheet = false
                        }
                    }
                }
            }
        }
    }
}
