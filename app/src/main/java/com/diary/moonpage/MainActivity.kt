package com.diary.moonpage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.diary.moonpage.core.theme.MoonPageTheme
import com.diary.moonpage.core.util.LocaleUtils
import com.diary.moonpage.core.util.SettingsPreferencesManager
import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.data.remote.api.SpotifyApi
import com.diary.moonpage.presentation.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var settingsPreferencesManager: SettingsPreferencesManager

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
            val isDark = isDarkModePref ?: isSystemInDarkTheme()
            val snackbarMessage by mainViewModel.snackbarMessage.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            
            val language by settingsPreferencesManager.language.collectAsState(initial = "en")

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

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && uri.scheme == "moonpage" && uri.host == "spotify-callback") {
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
                            }
                        } catch (e: Exception) {
                            mainViewModel.showSnackbar("Connection error: ${e.message}")
                        }
                    }
                }
            }
        }
    }
}
