package com.diary.moonpage.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diary.moonpage.core.theme.MoonPageTheme
import com.diary.moonpage.ui.components.buttons.MoonPrimaryButton
import com.diary.moonpage.ui.components.layout.MoonPageIndicator
import com.diary.moonpage.R
import com.diary.moonpage.ui.screens.auth.AuthViewModel
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LandingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val slideAnimationSpec = tween<Float>(
        durationMillis = 520,
        easing = FastOutSlowInEasing
    )
    val settledPage by remember { derivedStateOf { pagerState.settledPage } }
    val introPage by remember { derivedStateOf { pagerState.targetPage.coerceIn(0, 3) } }
    val isVietnamese = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        .toLanguageTags()
        .startsWith("vi")
    val currentLanguageLabel = if (isVietnamese) {
        stringResource(R.string.language_vietnamese)
    } else {
        stringResource(R.string.language_english)
    }
    val termsText = stringResource(R.string.terms_short)
    val privacyText = stringResource(R.string.privacy_short)
    val termsPrivacySeparator = stringResource(R.string.terms_privacy_separator)

    // Auto-scroll logic: Loops back to start after the last slide
    LaunchedEffect(settledPage) {
        val duration = when (settledPage) {
            0 -> 8000L // Slide 1: Mood Logging (complex animation)
            1 -> 3600L // Slide 2: Beautiful Logging (single screen)
            2 -> 9200L // Slide 3: Monthly Themes (5 themes)
            3 -> 7600L // Slide 4: Advanced Stats (2 frames)
            else -> 6000L
        }
        delay(duration)
        scope.launch {
            val nextPage = (settledPage + 1) % 4
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = slideAnimationSpec
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Spacer(modifier = Modifier.height(24.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge.copy(
                    shadow = Shadow(color = Color(0x33000000), offset = Offset(0f, 4f), blurRadius = 8f)
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.my_special_day),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        LandingSlideIntro(
            currentPage = introPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- CENTRAL PREVIEW BOX (only this part slides) ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (page) {
                    0 -> MoodLoggingSlide(isVisible = pagerState.currentPage == 0, showHeader = false)
                    1 -> PhotoLogSlide(isVisible = pagerState.currentPage == 1, showHeader = false)
                    2 -> AnnualLookBackSlide(isVisible = pagerState.currentPage == 2, showHeader = false)
                    3 -> AdvancedStatsSlide(isVisible = pagerState.currentPage == 3, showHeader = false)
                }
            }
        }

        // --- INDICATOR ---
        MoonPageIndicator(
            pageCount = 4,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // --- FOOTER ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language Selection & Terms
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Dropdown
                var showLanguageMenu by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showLanguageMenu = true }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Language,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentLanguageLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Icon(
                        Icons.Rounded.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.language_english)) },
                            onClick = { 
                                onLanguageChange("en")
                                showLanguageMenu = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.language_vietnamese)) },
                            onClick = { 
                                onLanguageChange("vi")
                                showLanguageMenu = false 
                            }
                        )
                    }
                }

                // Terms & Privacy
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append(termsText)
                        }
                        append(termsPrivacySeparator)
                        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append(privacyText)
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Wide Next Button
            MoonPrimaryButton(
                text = if (pagerState.currentPage == 3) stringResource(R.string.get_started) else stringResource(R.string.continue_btn),
                onClick = {
                    if (pagerState.currentPage < 3) {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = slideAnimationSpec
                            )
                        }
                    } else {
                        onNavigateToRegister()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link (keeping the flow)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.already_have_account),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = stringResource(R.string.login),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LandingSlideIntro(
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    val slideTexts = listOf(
        stringResource(R.string.onboarding_simple_diary_title) to stringResource(R.string.onboarding_simple_diary_desc),
        stringResource(R.string.onboarding_beautiful_logging_title) to stringResource(R.string.onboarding_beautiful_logging_desc),
        stringResource(R.string.onboarding_monthly_themes_title) to stringResource(R.string.onboarding_monthly_themes_desc),
        stringResource(R.string.onboarding_learn_about_you_title) to stringResource(R.string.onboarding_learn_about_you_desc)
    )

    Crossfade(
        targetState = currentPage.coerceIn(slideTexts.indices),
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "landing_slide_intro",
        modifier = modifier.height(92.dp)
    ) { page ->
        val (title, description) = slideTexts[page]
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
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
            onNavigateToRegister = {},
            onLanguageChange = {}
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
            onNavigateToRegister = {},
            onLanguageChange = {}
        )
    }
}
