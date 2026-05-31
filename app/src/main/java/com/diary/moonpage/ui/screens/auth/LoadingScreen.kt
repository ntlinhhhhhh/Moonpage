package com.diary.moonpage.ui.screens.auth

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonPageTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

@Composable
fun LoadingScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onFinished: (isLoggedIn: Boolean, needsOnboarding: Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        val token = viewModel.tokenFlow.first()
        val isLoggedIn = !token.isNullOrBlank()

        if (isLoggedIn) {
            viewModel.loadInitialAppResources {
                viewModel.viewModelScope.launch {
                    delay(300)
                    val onboardingDone = viewModel.checkOnboardingForCurrentUser()
                    onFinished(true, !onboardingDone)
                }
            }
        } else {
            delay(1500)
            onFinished(false, false)
        }
    }

    val isAppDark = com.diary.moonpage.core.theme.MoonTheme.customColors.isDark
    val backgroundColor = if (isAppDark) com.diary.moonpage.core.theme.MoonBgDark else com.diary.moonpage.core.theme.MoonBgLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            val animatedProgress by animateFloatAsState(
                targetValue = uiState.loadingProgress,
                label = "loadingProgress"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary, 
                trackColor = MaterialTheme.colorScheme.surfaceVariant, 
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (uiState.loadingProgress < 1f) stringResource(R.string.loading_feelings) else stringResource(R.string.ready_to_reflect),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun LoadingScreenLightPreview() {
    MoonPageTheme {
        LoadingScreen(onFinished = { _, _ -> })
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun LoadingScreenDarkPreview() {
    MoonPageTheme {
        LoadingScreen(onFinished = { _, _ -> })
    }
}
