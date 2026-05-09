package com.diary.moonpage.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diary.moonpage.R
import com.diary.moonpage.presentation.components.core.layout.MoonPageIndicator
import com.diary.moonpage.presentation.components.core.buttons.MoonPrimaryButton
import com.diary.moonpage.presentation.components.core.navigation.OnboardingPage
import com.diary.moonpage.presentation.components.core.navigation.OnboardingPageItem
import com.diary.moonpage.presentation.theme.MoonPageTheme

val onboardingPages = listOf(
    OnboardingPage(
        title = "Share moment",
        subtitle = "Gently observe your emotions and\nfind your daily center.",
        imageRes = R.drawable.logo,
        cardTitle = "Track your daily vibes",
        cardDescription = "Log your mood with organic shapes\nand soft colors that mirror your internal state."
    ),
    OnboardingPage(
        title = "Self reflection",
        subtitle = "Take a moment to breathe and\nlook deep inside your soul.",
        imageRes = R.drawable.logo,
        cardTitle = "Understand yourself",
        cardDescription = "Review your past entries to see how\nyou have grown over time."
    ),
    OnboardingPage(
        title = "Self reflection",
        subtitle = "Take a moment to breathe and\nlook deep inside your soul.",
        imageRes = R.drawable.logo,
        cardTitle = "Understand yourself",
        cardDescription = "Review your past entries to see how\nyou have grown over time."
    ),
    OnboardingPage(
        title = "Monthly statistics",
        subtitle = "A beautiful overview of your\nemotional journey.",
        imageRes = R.drawable.logo,
        cardTitle = "Visualize your mood",
        cardDescription = "Get insights into your feelings through\nbeautifully crafted charts."
    )
)

@Composable
fun LandingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Moonpage",
            style = MaterialTheme.typography.displayLarge.copy(
                shadow = Shadow(color = Color(0x33000000), offset = Offset(0f, 4f), blurRadius = 8f)
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "My special day",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageItem(
                pageData = onboardingPages[page]
            )
        }

        MoonPageIndicator(
            pageCount = onboardingPages.size,
            currentPage = pagerState.currentPage
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MoonPrimaryButton(
                text = "Register",
                onClick = onNavigateToRegister
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Login here",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToLogin() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    MoonPageTheme {
        LandingScreen(
            onNavigateToLogin = {},
             onNavigateToRegister = {}
        )
    }
}


@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LandingScreenPreviewDarkMode() {
    MoonPageTheme {
        LandingScreen(
            onNavigateToLogin = {},
            onNavigateToRegister = {}
        )
    }
}
