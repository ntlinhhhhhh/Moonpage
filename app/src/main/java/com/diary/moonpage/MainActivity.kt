package com.diary.moonpage

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.diary.moonpage.core.theme.MoonPageTheme
import com.diary.moonpage.core.util.LocaleUtils
import com.diary.moonpage.core.util.SettingsPreferencesManager
import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.data.remote.api.SpotifyApi
import com.diary.moonpage.presentation.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            android.util.Log.d("Notifications", "Permission granted")
        } else {
            android.util.Log.d("Notifications", "Permission denied")
        }
    }

    @Inject
    lateinit var settingsPreferencesManager: SettingsPreferencesManager

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var spotifyApi: SpotifyApi

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        requestNotificationPermission()

        splashScreen.setKeepOnScreenCondition {
            !mainViewModel.isReady.value
        }
        
        handleIntent(intent)

        // Lock app on start if enabled
        lifecycleScope.launch {
            if (settingsPreferencesManager.isPasscodeEnabled.first()) {
                mainViewModel.setLocked(true)
            }
        }

        // App lock observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                // When app goes to background, set locked to true if enabled
                lifecycleScope.launch {
                    if (settingsPreferencesManager.isPasscodeEnabled.first()) {
                        mainViewModel.setLocked(true)
                    }
                }
            }
        })

        setContent {
            val isReady by mainViewModel.isReady.collectAsState()
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
                CompositionLocalProvider(
                    com.diary.moonpage.core.theme.LocalLocale provides language
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (isReady) {
                                AppNavigation()
                            }
                            
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleUtils.getSavedLanguage(newBase)
        super.attachBaseContext(LocaleUtils.applyLocale(newBase, lang))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        android.util.Log.d("SpotifyAuth", "handleIntent called with URI: $uri and action: ${intent?.action}")
        if (uri != null && uri.scheme == "moonpage") {
            android.util.Log.d("SpotifyAuth", "Received URI: $uri")
            if (uri.host == "spotify-callback") {
                val code = uri.getQueryParameter("code")
                val error = uri.getQueryParameter("error")
                
                if (error != null) {
                    android.util.Log.e("SpotifyAuth", "Spotify returned error: $error")
                    android.widget.Toast.makeText(this, "Spotify Error: $error", android.widget.Toast.LENGTH_LONG).show()
                    mainViewModel.showSnackbar("Spotify Error: $error")
                    return
                }

                if (code != null) {
                    android.util.Log.d("SpotifyAuth", "Code received: $code")
                    lifecycleScope.launch {
                        val verifier = tokenManager.getSpotifyVerifier()
                        android.util.Log.d("SpotifyAuth", "Verifier from storage: $verifier")
                        
                        if (verifier != null) {
                            try {
                                val response = spotifyApi.exchangeToken(
                                    clientId = SpotifyApi.CLIENT_ID,
                                    code = code,
                                    redirectUri = SpotifyApi.REDIRECT_URI,
                                    codeVerifier = verifier
                                )
                                if (response.isSuccessful && response.body() != null) {
                                    val body = response.body()!!
                                    tokenManager.saveSpotifyToken(
                                        token = "Bearer ${body.accessToken}",
                                        refreshToken = body.refreshToken,
                                        expiresIn = body.expiresIn
                                    )
                                    android.util.Log.d("SpotifyAuth", "Token exchange successful!")
                                    android.widget.Toast.makeText(this@MainActivity, "Spotify Linked!", android.widget.Toast.LENGTH_SHORT).show()
                                    mainViewModel.showSnackbar("Spotify linked successfully!")
                                } else {
                                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                                    android.util.Log.e("SpotifyAuth", "Token exchange failed: $errorBody")
                                    android.widget.Toast.makeText(this@MainActivity, "Exchange Failed: $errorBody", android.widget.Toast.LENGTH_LONG).show()
                                    mainViewModel.showSnackbar("Token exchange failed: $errorBody")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("SpotifyAuth", "API Error", e)
                                android.widget.Toast.makeText(this@MainActivity, "API Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                mainViewModel.showSnackbar("API Error: ${e.message}")
                            }
                        } else {
                            android.util.Log.e("SpotifyAuth", "Missing verifier in storage!")
                            android.widget.Toast.makeText(this@MainActivity, "Error: Missing Verifier", android.widget.Toast.LENGTH_LONG).show()
                            mainViewModel.showSnackbar("Error: Missing local verifier")
                        }
                    }
                } else {
                    android.util.Log.e("SpotifyAuth", "No code found in URI")
                    mainViewModel.showSnackbar("Error: No code received from Spotify")
                }
            } else {
                android.util.Log.e("SpotifyAuth", "Unknown host: ${uri.host}")
                mainViewModel.showSnackbar("Error: Unknown host ${uri.host}")
            }
        }
    }
}
