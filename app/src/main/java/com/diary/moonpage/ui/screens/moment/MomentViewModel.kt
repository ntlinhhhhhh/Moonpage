package com.diary.moonpage.ui.screens.moment

import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.LocationTracker
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.domain.repository.MomentRepository
import com.diary.moonpage.domain.repository.WeatherRepository
import com.diary.moonpage.domain.usecase.moment.*
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
    private val dailyLogRepository: com.diary.moonpage.domain.repository.DailyLogRepository,
    private val weatherRepository: com.diary.moonpage.domain.repository.WeatherRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MomentUiState())
    val uiState: StateFlow<MomentUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<MomentUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    private val locationTracker = com.diary.moonpage.core.util.LocationTracker(
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context),
        context
    )

    init {
        observeRepository()
        onEvent(MomentUiEvent.LoadMoments)
        triggerAutoWeatherFetch()
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            try {
                val location = locationTracker.getCurrentLocation()
                if (location != null) {
                    val today = java.time.LocalDate.now()
                    weatherRepository.getWeatherConditions(location.latitude, location.longitude, today).onSuccess { result ->
                        val conditions = result.conditions
                        val temp = result.averageTemp
                        
                        val weatherText = if (conditions.isNotEmpty()) {
                            val mainCondition = conditions.first()
                            val tempText = String.format(java.util.Locale.ENGLISH, "%.1f°C", temp)
                            "$mainCondition, $tempText"
                        } else {
                            "Unknown"
                        }

                        _uiState.update { it.copy(
                            suggestedWeather = com.diary.moonpage.domain.repository.WeatherData(
                                condition = weatherText,
                                description = context.getString(R.string.weather_auto_filled),
                                temp = temp,
                                cityName = context.getString(R.string.detected),
                                iconUrl = ""
                            )
                        ) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MomentVM", "Weather fetch failed", e)
            }
        }
    }

    fun triggerAutoWeatherFetch() {
        fetchWeather()
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
            MomentUiEvent.RefreshWeather -> triggerAutoWeatherFetch()
        }
    }

    private fun fetchMyMoments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMyMomentsUseCase().onFailure { error ->
                _uiEffect.send(MomentUiEffect.ShowSnackBar(error.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.error_unknown)))
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
                _uiEffect.send(MomentUiEffect.ShowSnackBar(error.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.error_unknown)))
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
        val dateOnlyStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val actualLogId = if (dailyLogId == "default_log_id") dateOnlyStr else dailyLogId

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            uploadMomentUseCase(
                actualLogId,
                imageFile,
                caption,
                isPublic,
                capturedAtStr,
                location,
                weather,
                rating
            ).onSuccess {
                _uiState.update { it.copy(isUploading = false) }
                
                // Refresh local cache of logs so the new photo appears in DailyLog screen
                viewModelScope.launch {
                    dailyLogRepository.getDailyLogByDate(actualLogId)
                }

                _uiEffect.send(MomentUiEffect.UploadSuccess)
                _uiEffect.send(MomentUiEffect.ShowSnackBar(UiText.StringResource(R.string.moment_upload_success)))
            }.onFailure { error ->
                _uiState.update { it.copy(isUploading = false) }
                _uiEffect.send(MomentUiEffect.ShowSnackBar(error.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.upload_failed)))
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
                _uiEffect.send(MomentUiEffect.ShowSnackBar(error.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.delete_failed)))
            }
        }
    }
}
