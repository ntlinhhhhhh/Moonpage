package com.diary.moonpage.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.diary.moonpage.core.util.LocaleUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val language = getSavedLanguage(newBase)
        super.attachBaseContext(LocaleUtils.applyLocale(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val language = getSavedLanguage(this)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(mainViewModel)
        splashScreen.setKeepOnScreenCondition { !mainViewModel.uiState.value.isReady }
        
        mainViewModel.handleSpotifyIntent(intent.data)

        setContent {
            MoonPageApp(mainViewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        mainViewModel.handleSpotifyIntent(intent.data)
    }

    private fun getSavedLanguage(context: Context): String {
        return context
            .getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .getString("language", "en") ?: "en"
    }
}
