package com.diary.moonpage.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.diary.moonpage.core.util.LocaleUtils
import com.diary.moonpage.widget.glance.MoonpageWidgets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private var widgetTargetRoute by mutableStateOf<String?>(null)

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
        
        widgetTargetRoute = intent.widgetTargetRoute()
        mainViewModel.handleSpotifyIntent(intent.data)
        intent.setData(null)

        setContent {
            MoonPageApp(
                viewModel = mainViewModel,
                widgetTargetRoute = widgetTargetRoute,
                onWidgetTargetRouteConsumed = { widgetTargetRoute = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        widgetTargetRoute = intent.widgetTargetRoute()
        mainViewModel.handleSpotifyIntent(intent.data)
        this.intent.setData(null)
    }

    private fun getSavedLanguage(context: Context): String {
        return context
            .getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .getString("language", "en") ?: "en"
    }

    private fun Intent.widgetTargetRoute(): String? {
        return getStringExtra(MoonpageWidgets.EXTRA_TARGET_ROUTE)
    }
}
