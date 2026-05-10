package com.diary.moonpage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.diary.moonpage.presentation.navigation.AppNavigation
import com.diary.moonpage.core.theme.MoonPageTheme
import dagger.hilt.android.AndroidEntryPoint

import android.content.Intent
import com.diary.moonpage.core.util.TokenManager
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.diary.moonpage.data.remote.api.SpotifyApi
import kotlinx.coroutines.flow.first

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var spotifyApi: SpotifyApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            val themeType by mainViewModel.themeType.collectAsState()
            val isDarkModePref by mainViewModel.isDarkMode.collectAsState()
            val isDark = isDarkModePref ?: androidx.compose.foundation.isSystemInDarkTheme()
            val snackbarMessage by mainViewModel.snackbarMessage.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(snackbarMessage) {
                snackbarMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    mainViewModel.dismissSnackbar()
                }
            }

            MoonPageTheme(
                themeType = themeType,
                darkTheme = isDark
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { padding ->
                    AppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && uri.scheme == "moonpage" && uri.host == "spotify-callback") {
            val error = uri.getQueryParameter("error")
            if (error != null) {
                return
            }

            val code = uri.getQueryParameter("code")
            if (code != null) {
                lifecycleScope.launch {
                    val verifier = tokenManager.getSpotifyVerifier()
                    if (verifier != null) {
                        try {
                            val response = spotifyApi.exchangeToken(
                                clientId = SpotifyApi.CLIENT_ID,
                                code = code,
                                redirectUri = SpotifyApi.REDIRECT_URI,
                                codeVerifier = verifier
                            )
                            if (response.isSuccessful && response.body() != null) {
                                val token = response.body()!!.accessToken
                                tokenManager.saveSpotifyToken("Bearer $token")
                                mainViewModel.showSnackbar("Spotify linked successfully!")
                            } else {
                                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                                mainViewModel.showSnackbar("Failed to link Spotify: $errorMsg")
                            }
                        } catch (e: Exception) {
                            mainViewModel.showSnackbar("Connection error: ${e.message}")
                        }
                    } else {
                        mainViewModel.showSnackbar("Error: Auth verifier lost. Please try again.")
                    }
                }
            }
        }
    }
}
