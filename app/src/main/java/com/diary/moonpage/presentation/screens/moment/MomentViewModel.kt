package com.diary.moonpage.presentation.screens.moment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.domain.repository.MomentRepository
import com.diary.moonpage.domain.usecase.moment.*
import com.diary.moonpage.presentation.components.moment.MomentTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
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

    val allTags = listOf(
        MomentTag("text", null, "Message"),
        MomentTag("review", Icons.Rounded.Star, "Review"),
        MomentTag("location", Icons.Rounded.LocationOn, "Location"),
        MomentTag("weather", Icons.Rounded.WbSunny, "Weather"),
        MomentTag("party", null, "Party Time!", containerColor = Color(0xFF80FFE8), contentColor = Color.Black),
        MomentTag("ootd", null, "OOTD", containerColor = Color.White, contentColor = Color.Black),
        MomentTag("missyou", null, "Miss you", containerColor = Color(0xFFFF4B4B), contentColor = Color.White)
    )

    init {
        observeRepository()
        onEvent(MomentUiEvent.LoadMoments)
        fetchWeather()
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            try {
                val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            viewModelScope.launch {
                                weatherRepository.getCurrentWeather(location.latitude, location.longitude).onSuccess { data ->
                                    _uiState.update { it.copy(suggestedWeather = data) }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {}
        }
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
                
                // Automatically attach to daily log if it exists
                viewModelScope.launch {
                    dailyLogRepository.getDailyLogByDate(dateOnlyStr).onSuccess { log ->
                        // Add the photo to the log. 
                        // Note: DailyLogRepository.createDailyLog takes a list of Files.
                        // We have the current imageFile.
                        dailyLogRepository.createDailyLog(
                            baseMoodId = log.baseMoodId,
                            date = log.date,
                            note = log.note,
                            sleepHours = log.sleepHours,
                            sleepStartTime = log.sleepStartTime,
                            isMenstruation = log.isMenstruation,
                            menstruationPhase = log.menstruationPhase,
                            activityIds = log.activityIds,
                            dailyPhotos = listOf(imageFile), // The repository handles merging/uploading
                            steps = log.steps,
                            musicRecord = log.musicRecord,
                            calories = log.calories,
                            distance = log.distance
                        )
                    }
                }
                
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
