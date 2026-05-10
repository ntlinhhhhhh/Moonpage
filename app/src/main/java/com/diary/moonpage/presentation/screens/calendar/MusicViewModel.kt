package com.diary.moonpage.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.data.remote.api.SpotifyApi
import com.diary.moonpage.data.remote.api.SpotifyTrack
import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.core.util.PkceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

data class MusicUiState(
    val searchQuery: String = "",
    val searchResults: List<SpotifyTrack> = emptyList(),
    val suggestions: List<SpotifyTrack> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLinked: Boolean = false
)

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val spotifyApi: SpotifyApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var currentToken: String? = null

    init {
        viewModelScope.launch {
            tokenManager.getSpotifyToken().collectLatest { token ->
                currentToken = token
                _uiState.update { it.copy(isLinked = token != null) }
                if (token != null) {
                    fetchRecentlyPlayed()
                }
            }
        }
    }

    private fun fetchRecentlyPlayed() {
        val token = currentToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = spotifyApi.getRecentlyPlayedTracks(token)
                if (response.isSuccessful) {
                    val tracks = response.body()?.items?.map { it.track } ?: emptyList()
                    _uiState.update { it.copy(suggestions = tracks, isLoading = false) }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.update { it.copy(
                        error = "Load Failed (${response.code()}): $errorBody",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Connection error: ${e.message}", isLoading = false) }
            }
        }
    }

    suspend fun getSpotifyAuthUrl(): String {
        val verifier = PkceUtil.generateCodeVerifier()
        val challenge = PkceUtil.generateCodeChallenge(verifier)
        val state = UUID.randomUUID().toString()
        
        tokenManager.saveSpotifyAuthData(verifier, state)
        
        return SpotifyApi.getAuthUrl(challenge, state)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.length > 2 && currentToken != null) {
            searchMusic(query)
        } else if (query.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    private fun searchMusic(query: String) {
        val token = currentToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = spotifyApi.searchTracks(token, query)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(
                        searchResults = response.body()?.tracks?.items ?: emptyList(),
                        isLoading = false
                    ) }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.update { it.copy(
                        error = "Search Failed (${response.code()}): $errorBody",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
