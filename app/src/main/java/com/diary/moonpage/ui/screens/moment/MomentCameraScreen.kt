package com.diary.moonpage.ui.screens.moment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.diary.moonpage.R
import com.diary.moonpage.ui.components.feedback.GlobalSnackbarManager
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import com.diary.moonpage.ui.screens.moment.components.CameraMainUI
import com.diary.moonpage.ui.screens.moment.components.MomentTag
import com.diary.moonpage.ui.screens.moment.components.TagChip
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
fun MomentCameraRoute(
    initialMomentId: String? = null,
    onNavigateToGallery: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        // Preview placeholder to avoid Hilt crash
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val viewModel: MomentViewModel = hiltViewModel()
    val profileViewModel: com.diary.moonpage.ui.screens.profile.ProfileViewModel = hiltViewModel()

    MomentCameraScreen(
        initialMomentId = initialMomentId,
        onNavigateToGallery = onNavigateToGallery,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToAccount = onNavigateToAccount,
        viewModel = viewModel,
        profileViewModel = profileViewModel
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MomentCameraScreen(
    initialMomentId: String? = null,
    onNavigateToGallery: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAccount: () -> Unit,
    viewModel: MomentViewModel,
    profileViewModel: com.diary.moonpage.ui.screens.profile.ProfileViewModel
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
    val snackbarHostState = remember { SnackbarHostState() }
    val allTags = rememberMomentUploadTags()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                MomentUiEffect.UploadSuccess -> {
                    isSuccess = true
                }
                is MomentUiEffect.ShowSnackBar -> {
                    GlobalSnackbarManager.show(effect.message.asString(context))
                }
                is MomentUiEffect.ShareMoment -> {
                    coroutineScope.launch {
                        val request = ImageRequest.Builder(context)
                            .data(effect.url)
                            .build()
                        val result = context.imageLoader.execute(request)
                        if (result is coil.request.SuccessResult) {
                            val bitmap = (result.drawable as android.graphics.drawable.BitmapDrawable).bitmap
                            com.diary.moonpage.core.util.ImageUtils.shareImage(context, bitmap, context.getString(R.string.share_moment))
                        } else {
                            GlobalSnackbarManager.show(context.getString(R.string.failed_to_load_image_for_sharing))
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
            allTags = allTags,
            locationPermissionState = locationPermissionState,
            onEvent = viewModel::onEvent,
            onNavigateToGallery = onNavigateToGallery,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToAccount = onNavigateToAccount,
            initialMomentId = initialMomentId,
            snackbarHostState = snackbarHostState,
            avatarUrl = profileState.user?.avatarUrl,
            localAvatarPath = profileState.localAvatarPath ?: profileState.tempAvatarPath,
            isSuccess = isSuccess,
            onResetSuccess = { isSuccess = false }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.camera_permission_required), color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun rememberMomentUploadTags(): List<MomentTag> {
    val message = stringResource(R.string.moment_tag_message)
    val review = stringResource(R.string.moment_tag_review)
    val location = stringResource(R.string.moment_tag_location)
    val weather = stringResource(R.string.moment_tag_weather)
    val partyTime = stringResource(R.string.moment_tag_party_time)
    val ootd = stringResource(R.string.moment_tag_ootd)
    val missYou = stringResource(R.string.moment_tag_miss_you)

    return remember(message, review, location, weather, partyTime, ootd, missYou) {
        listOf(
            MomentTag("text", null, message),
            MomentTag("review", Icons.Rounded.Star, review),
            MomentTag("location", Icons.Rounded.LocationOn, location),
            MomentTag("weather", Icons.Rounded.WbSunny, weather),
            MomentTag("party", null, partyTime, containerColor = Color(0xFF80FFE8), contentColor = Color.Black),
            MomentTag("ootd", null, ootd, containerColor = Color.White, contentColor = Color.Black),
            MomentTag("missyou", null, missYou, containerColor = Color(0xFFFF4B4B), contentColor = Color.White)
        )
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
    onNavigateToAccount: () -> Unit,
    initialMomentId: String? = null,
    snackbarHostState: SnackbarHostState,
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
    
    // Scroll to history if coming from gallery with a specific moment
    LaunchedEffect(initialMomentId) {
        if (initialMomentId != null) {
            verticalPagerState.scrollToPage(1)
        }
    }

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
    val defaultWeather = stringResource(R.string.weather_sunny_icon)
    var userWeather by remember(defaultWeather) { mutableStateOf(defaultWeather) }
    var showTagSheet by remember { mutableStateOf(false) }
    var pendingLocationRequest by remember { mutableStateOf(false) }

    val uploadPagerState = rememberPagerState(pageCount = { allTags.size })

    val reverseGeocode = { location: Location ->
        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val district = address.subLocality ?: address.locality ?: address.subAdminArea
                    val city = address.adminArea ?: address.locality ?: context.getString(R.string.unknown)
                    val locationName = if (district != null && district != city) "$district/$city" else city
                    withContext(Dispatchers.Main) { userLocation = locationName }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { userLocation = context.getString(R.string.location_error) }
            }
        }
    }

    val fetchLocationFast = {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            location?.let { reverseGeocode(it) }
        }
    }

    fun isGpsEnabled(ctx: android.content.Context): Boolean {
        val lm = ctx.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    var showGpsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted && pendingLocationRequest) {
            pendingLocationRequest = false
            if (isGpsEnabled(context)) {
                fetchLocationFast()
                onEvent(MomentUiEvent.RefreshWeather)
            } else {
                showGpsDialog = true
            }
        }
    }

    LaunchedEffect(uiState.suggestedWeather) {
        uiState.suggestedWeather?.let { weather ->
            val icon = when {
                weather.condition.contains("Sunny") -> "☀️"
                weather.condition.contains("Cloudy") -> "☁️"
                weather.condition.contains("Rainy") -> "🌧️"
                weather.condition.contains("Snowy") -> "❄️"
                weather.condition.contains("Windy") -> "💨"
                weather.condition.contains("Stormy") -> "⛈️"
                else -> "🌡️"
            }
            userWeather = "${weather.condition} $icon"
        }
    }

    Scaffold() { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
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
                            avatarUrl = avatarUrl,
                            onAvatarClick = onNavigateToAccount
                        )
                    } else {
                        MomentHistoryScreen(
                            moments = uiState.moments,
                            localPaths = uiState.localPaths,
                            onNavigateToGallery = onNavigateToGallery,
                            onBackToCamera = { scope.launch { verticalPagerState.animateScrollToPage(0) } },
                            onNavigateToAccount = onNavigateToAccount,
                            initialMomentId = initialMomentId,
                            onShare = { moment -> onEvent(MomentUiEvent.ShareMoment(moment.imageUrl)) },
                            onDownload = { moment -> onEvent(MomentUiEvent.DownloadMoment(moment.imageUrl)) },
                            onDelete = { moment -> onEvent(MomentUiEvent.DeleteMoment(moment.id)) },
                            avatarUrl = avatarUrl,
                            localAvatarPath = localAvatarPath,
                            isVerticalVisible = verticalPagerState.currentPage == 1
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
                        if (!locationPermissionState.allPermissionsGranted) {
                            pendingLocationRequest = true
                            locationPermissionState.launchMultiplePermissionRequest()
                        } else if (!isGpsEnabled(context)) {
                            showGpsDialog = true
                        } else {
                            onEvent(MomentUiEvent.RefreshWeather)
                        }
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

            if (showGpsDialog) {
                AlertDialog(
                    onDismissRequest = { showGpsDialog = false },
                    containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.popupBgColor,
                    title = { Text(stringResource(R.string.location_services_off)) },
                    text = { Text(stringResource(R.string.location_services_add_location_desc)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showGpsDialog = false
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)) { Text(stringResource(R.string.open_settings)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showGpsDialog = false }, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnTextColor)) { Text(stringResource(R.string.cancel)) }
                    }
                )
            }
        }
    }
}
