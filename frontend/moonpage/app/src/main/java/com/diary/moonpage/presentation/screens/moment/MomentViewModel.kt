package com.diary.moonpage.presentation.screens.moment

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.domain.model.Moment
import com.diary.moonpage.domain.usecase.moment.*
import com.diary.moonpage.presentation.components.moment.MomentTag
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private val Context.momentDataStore by preferencesDataStore(name = "moment_cache")

@HiltViewModel
class MomentViewModel @Inject constructor(
    private val getMyMomentsUseCase: GetMyMomentsUseCase,
    private val getMomentUseCase: GetMomentUseCase,
    private val uploadMomentUseCase: UploadMomentUseCase,
    private val deleteMomentUseCase: DeleteMomentUseCase,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MomentUiState())
    val uiState: StateFlow<MomentUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<MomentUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val MOMENT_LIST_KEY = stringPreferencesKey("cached_moments")

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
        loadCachedMoments()
        onEvent(MomentUiEvent.LoadMoments)
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
                event.isPublic,
                event.onSuccess
            )
            is MomentUiEvent.DeleteMoment -> deleteMoment(event.id)
            is MomentUiEvent.DownloadMoment -> downloadMoment(event.imageUrl)
            is MomentUiEvent.ShareMoment -> shareMoment(event.url)
            MomentUiEvent.DismissMessage -> _uiState.update { it.copy(errorMessage = null, successMessage = null) }
            is MomentUiEvent.ShowSnackBar -> viewModelScope.launch { _uiEvent.send(event) }
        }
    }

    private fun loadCachedMoments() {
        viewModelScope.launch {
            try {
                val preferences = context.momentDataStore.data.first()
                val json = preferences[MOMENT_LIST_KEY]
                if (!json.isNullOrEmpty()) {
                    val type = object : TypeToken<List<Moment>>() {}.type
                    val cachedList: List<Moment> = gson.fromJson(json, type)
                    _uiState.update { it.copy(moments = cachedList) }
                }
            } catch (e: Exception) {
                Log.e("MomentVM", "Error loading cache", e)
            }
        }
    }

    private fun saveMomentsToCache(list: List<Moment>) {
        viewModelScope.launch {
            try {
                val json = gson.toJson(list)
                context.momentDataStore.edit { prefs ->
                    prefs[MOMENT_LIST_KEY] = json
                }
            } catch (e: Exception) {
                Log.e("MomentVM", "Error saving cache", e)
            }
        }
    }

    private fun fetchMyMoments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMyMomentsUseCase().onSuccess { newList ->
                _uiState.update { it.copy(isLoading = false, moments = newList) }
                saveMomentsToCache(newList)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = UiText.DynamicString(error.message ?: "Unknown error")) }
            }
        }
    }

    private fun fetchMomentDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMomentUseCase(id).onSuccess { moment ->
                _uiState.update { it.copy(isLoading = false, selectedMoment = moment) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = UiText.DynamicString(error.message ?: "Unknown error")) }
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
        isPublic: Boolean,
        onSuccess: () -> Unit
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val capturedAtStr = sdf.format(Date())

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            try {
                val imagePart = MultipartBody.Part.createFormData(
                    "imageFile",
                    imageFile.name,
                    imageFile.asRequestBody("image/webp".toMediaTypeOrNull())
                )
                
                val dailyLogIdBody = dailyLogId.toRequestBody("text/plain".toMediaTypeOrNull())
                val captionBody = caption.toRequestBody("text/plain".toMediaTypeOrNull())
                val isPublicBody = isPublic.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val capturedAtBody = capturedAtStr.toRequestBody("text/plain".toMediaTypeOrNull())
                val locationBody = location?.toRequestBody("text/plain".toMediaTypeOrNull())
                val weatherBody = weather?.toRequestBody("text/plain".toMediaTypeOrNull())
                val ratingBody = rating?.toString()?.toRequestBody("text/plain".toRequestBody("text/plain".toMediaTypeOrNull()).contentType()) // Corrected

                uploadMomentUseCase(
                    dailyLogIdBody,
                    imagePart,
                    captionBody,
                    isPublicBody,
                    capturedAtBody,
                    locationBody,
                    weatherBody,
                    rating?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                ).onSuccess { response ->
                    val fileName = "moment_${response.id}.webp"
                    val permanentFile = File(context.filesDir, "moments/$fileName")
                    permanentFile.parentFile?.mkdirs()
                    imageFile.copyTo(permanentFile, overwrite = true)
                    
                    _uiState.update { state ->
                        val updatedPaths = state.localPaths + (response.imageUrl to permanentFile.absolutePath)
                        val updatedList = (listOf(response) + state.moments).distinctBy { it.id }
                        state.copy(
                            isUploading = false,
                            localPaths = updatedPaths,
                            moments = updatedList,
                            successMessage = UiText.StringResource(R.string.moment_upload_success)
                        )
                    }
                    saveMomentsToCache(_uiState.value.moments)
                    onSuccess()
                }.onFailure { error ->
                    _uiState.update { it.copy(isUploading = false, errorMessage = UiText.DynamicString(error.message ?: "Upload failed")) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, errorMessage = UiText.DynamicString(e.message ?: "Error preparing upload")) }
            }
        }
    }

    private fun deleteMoment(id: String) {
        viewModelScope.launch {
            val momentToDelete = _uiState.value.moments.find { it.id == id }
            val localPath = momentToDelete?.let { _uiState.value.localPaths[it.imageUrl] }

            _uiState.update { it.copy(isLoading = true) }
            deleteMomentUseCase(id).onSuccess {
                // Xóa file local nếu có
                localPath?.let { path ->
                    try {
                        val file = File(path)
                        if (file.exists()) file.delete()
                    } catch (e: Exception) {
                        Log.e("MomentVM", "Error deleting local file", e)
                    }
                }

                _uiState.update { state ->
                    val updatedList = state.moments.filter { it.id != id }
                    val updatedPaths = state.localPaths.filter { (url, _) ->
                        updatedList.any { it.imageUrl == url }
                    }
                    state.copy(
                        isLoading = false,
                        moments = updatedList,
                        localPaths = updatedPaths,
                        successMessage = UiText.StringResource(R.string.moment_deleted)
                    )
                }
                saveMomentsToCache(_uiState.value.moments)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = UiText.DynamicString(error.message ?: "Delete failed")) }
            }
        }
    }

    private fun downloadMoment(imageUrl: String) {
        viewModelScope.launch {
            ImageUtils.downloadAndSaveImage(context, imageUrl)
        }
    }

    private fun shareMoment(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out my moment: $url")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Share Moment").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
