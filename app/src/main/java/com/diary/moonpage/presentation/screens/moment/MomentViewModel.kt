package com.diary.moonpage.presentation.screens.moment

import android.location.Geocoder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.LocationTracker
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.domain.repository.MomentRepository
import com.diary.moonpage.domain.repository.WeatherRepository
import com.diary.moonpage.domain.usecase.moment.*
import com.diary.moonpage.presentation.components.moment.MomentTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MomentViewModel @Inject constructor(
    private val repository: MomentRepository,
    private val getMyMomentsUseCase: GetMyMomentsUseCase,
    private val getMomentUseCase: GetMomentUseCase,
    private val uploadMomentUseCase: UploadMomentUseCase,
    private val deleteMomentUseCase: DeleteMomentUseCase,
    private val weatherRepository: WeatherRepository,
    private val locationTracker: LocationTracker,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MomentUiState())
    val uiState: StateFlow<MomentUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<MomentUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    val allTags = listOf(
        MomentTag("text", null, "Message"),
        MomentTag("review", Icons.Rounded.Star, "Review"),
        MomentTag("location", Icons.Rounded.LocationOn, "Location"),
        MomentTag("weather", Icons.Rounded.WbSunny, "Weather"),
        MomentTag("time", Icons.Rounded.AccessTime, SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())),
        MomentTag("party", null, "Party Time!", containerColor = Color(0xFF80FFE8), contentColor = Color.Black),
        MomentTag("ootd", null, "OOTD", containerColor = Color.White, contentColor = Color.Black),
        MomentTag("missyou", null, "Miss you", containerColor = Color(0xFFFF4B4B), contentColor = Color.White)
    )

    init {
        observeRepository()
        onEvent(MomentUiEvent.LoadMoments)
    }

    private fun observeRepository() {
        viewModelScope.launch {
            repository.moments.collect { list ->
                _uiState.update { it.copy(moments = list) }
            }
        }
        viewModelScope.launch {
            repository.localPaths.collect { paths ->
                _uiState.update { it.copy(localPaths = paths) }
            }
        }
    }

    fun onEvent(event: MomentUiEvent) {
        when (event) {
            is MomentUiEvent.LoadMoments -> fetchMyMoments()
            is MomentUiEvent.LoadMomentDetail -> fetchMomentDetail(event.id)
            is MomentUiEvent.UploadMoment -> uploadMoment(
                event.imageFile,
                event.caption,
                event.location,
                event.weather,
                event.rating,
                event.dailyLogId,
                event.isPublic
            )
            is MomentUiEvent.DeleteMoment -> deleteMoment(event.id)
            is MomentUiEvent.DownloadMoment -> viewModelScope.launch { _uiEffect.send(MomentUiEffect.DownloadMoment(event.imageUrl)) }
            is MomentUiEvent.ShareMoment -> viewModelScope.launch { _uiEffect.send(MomentUiEffect.ShareMoment(event.url)) }
            MomentUiEvent.DismissMessage -> _uiState.update { it.copy(errorMessage = null, successMessage = null) }
            is MomentUiEvent.ShowSnackBar -> viewModelScope.launch { _uiEffect.send(MomentUiEffect.ShowSnackBar(event.message)) }
            is MomentUiEvent.OnLocationPermissionGranted -> {
                autoFetchDetails()
            }
        }
    }

    private fun autoFetchDetails() {
        viewModelScope.launch {
            val location = locationTracker.getCurrentLocation()
            if (location != null) {
                // 1. Fetch Weather
                weatherRepository.getWeatherConditions(location.latitude, location.longitude, LocalDate.now())
                    .onSuccess { weatherNames ->
                        if (weatherNames.isNotEmpty()) {
                            val primaryWeather = weatherNames[0]
                            val emoji = when (primaryWeather) {
                                "Sunny" -> "☀️"
                                "Cloudy" -> "☁️"
                                "Rainy" -> "🌧️"
                                "Snowy" -> "❄️"
                                "Windy" -> "💨"
                                "Stormy" -> "⛈️"
                                "Hot" -> "🔥"
                                "Cold" -> "❄️"
                                else -> ""
                            }
                            val weatherText = if (emoji.isNotEmpty()) "$primaryWeather $emoji" else primaryWeather
                            _uiState.update { it.copy(autoWeather = weatherText) }
                        }
                    }

                // 2. Fetch Location Name
                launch(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val district = address.subLocality ?: address.locality ?: address.subAdminArea
                            val city = address.adminArea ?: address.locality ?: "Unknown"
                            val locationName = if (district != null && district != city) "$district/$city" else city
                            _uiState.update { it.copy(autoLocation = locationName) }
                        }
                    } catch (e: Exception) {
                        // Keep current or empty
                    }
                }
            }
        }
    }

    private fun fetchMyMoments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMyMomentsUseCase().onFailure { error ->
                _uiEffect.send(MomentUiEffect.ShowSnackBar(UiText.DynamicString(error.message ?: "Unknown error")))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun fetchMomentDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMomentUseCase(id).onSuccess { moment ->
                _uiState.update { it.copy(isLoading = false, selectedMoment = moment) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.send(MomentUiEffect.ShowSnackBar(UiText.DynamicString(error.message ?: "Unknown error")))
            }
        }
    }

    private fun uploadMoment(
        imageFile: File,
        caption: String,
        location: String?,
        weather: String?,
        rating: Float?,
        dailyLogId: String,
        isPublic: Boolean
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val capturedAtStr = sdf.format(Date())

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            uploadMomentUseCase(
                dailyLogId,
                imageFile,
                caption,
                isPublic,
                capturedAtStr,
                location,
                weather,
                rating
            ).onSuccess {
                _uiState.update { it.copy(isUploading = false) }
                _uiEffect.send(MomentUiEffect.UploadSuccess)
                _uiEffect.send(MomentUiEffect.ShowSnackBar(UiText.StringResource(R.string.moment_upload_success)))
            }.onFailure { error ->
                _uiState.update { it.copy(isUploading = false) }
                _uiEffect.send(MomentUiEffect.ShowSnackBar(UiText.DynamicString(error.message ?: "Upload failed")))
            }
        }
    }

    private fun deleteMoment(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            deleteMomentUseCase(id).onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.send(MomentUiEffect.ShowSnackBar(UiText.StringResource(R.string.moment_deleted)))
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.send(MomentUiEffect.ShowSnackBar(UiText.DynamicString(error.message ?: "Delete failed")))
            }
        }
    }
}
