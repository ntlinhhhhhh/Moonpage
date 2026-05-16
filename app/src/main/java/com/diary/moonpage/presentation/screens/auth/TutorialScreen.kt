package com.diary.moonpage.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.SettingsPreferencesManager
import com.diary.moonpage.presentation.components.core.buttons.MoonPrimaryButton
import com.diary.moonpage.presentation.components.core.layout.MoonPageIndicator
import com.diary.moonpage.presentation.components.core.navigation.OnboardingPage
import com.diary.moonpage.presentation.components.core.navigation.OnboardingPageItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val settingsPreferencesManager: SettingsPreferencesManager
) : ViewModel() {
    fun setTutorialCompleted() {
        viewModelScope.launch {
            settingsPreferencesManager.setTutorialCompleted(true)
        }
    }
}

val tutorialPages = listOf(
    OnboardingPage(
        title = "Choose Your Mood",
        subtitle = "Select from 5 organic shapes that best\nrepresent how you feel today.",
        imageRes = R.drawable.logo, // Replace with actual tutorial images if available
        cardTitle = "Tap to select",
        cardDescription = "From 'Awful' to 'Rad', each mood has\na unique color and motion."
    ),
    OnboardingPage(
        title = "Record Activities",
        subtitle = "What made your day? Tag hobbies,\nevents, or self-care routines.",
        imageRes = R.drawable.logo,
        cardTitle = "Tag your day",
        cardDescription = "Easily add icons for meals, work, or\nexercise to see patterns in your life."
    ),
    OnboardingPage(
        title = "Write & Reflect",
        subtitle = "Add a personal note or a photo to\ncapture the essence of the moment.",
        imageRes = R.drawable.logo,
        cardTitle = "Keep a memory",
        cardDescription = "Your diary is a safe space for your\nthoughts, dreams, and photos."
    )
)

@Composable
fun TutorialScreen(
    viewModel: TutorialViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tutorialPages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tutorial",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageItem(pageData = tutorialPages[page])
        }

        Spacer(modifier = Modifier.height(16.dp))

        MoonPageIndicator(
            pageCount = tutorialPages.size,
            currentPage = pagerState.currentPage
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            if (pagerState.currentPage == tutorialPages.size - 1) {
                MoonPrimaryButton(
                    text = "Get Started",
                    onClick = {
                        viewModel.setTutorialCompleted()
                        onFinish()
                    }
                )
            } else {
                MoonPrimaryButton(
                    text = "Next",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                )
            }
        }
    }
}
