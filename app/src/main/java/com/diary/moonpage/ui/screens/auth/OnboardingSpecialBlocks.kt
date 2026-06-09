package com.diary.moonpage.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonTheme

// ── Data model ────────────────────────────────────────────────────────────────

data class SpecialBlockPrefs(
    val musicEnabled: Boolean = false,
    val stepsEnabled: Boolean = false,
    val sleepEnabled: Boolean = false,
    val menstruationEnabled: Boolean = false
)

sealed class SpecialBlockConfig(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector
) {
    object Music       : SpecialBlockConfig("music",        R.string.special_block_music,         R.string.special_block_music_subtitle,         Icons.Rounded.MusicNote)
    object Steps       : SpecialBlockConfig("steps",        R.string.special_block_steps,         R.string.special_block_steps_subtitle,         Icons.Rounded.DirectionsWalk)
    object Sleep       : SpecialBlockConfig("sleep",        R.string.special_block_sleep,         R.string.special_block_sleep_subtitle,         Icons.Rounded.Nightlight)
    object Menstruation: SpecialBlockConfig("menstruation", R.string.special_block_menstruation,  R.string.special_block_menstruation_subtitle,  Icons.Rounded.FavoriteBorder)
}

private val NORMAL_BLOCKS = listOf(
    SpecialBlockConfig.Music,
    SpecialBlockConfig.Steps,
    SpecialBlockConfig.Sleep,
    SpecialBlockConfig.Menstruation
)

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * Stateful Component
 */
@Composable
fun OnboardingSpecialBlocksRoute(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNext: () -> Unit
) {
    var prefs by remember { mutableStateOf(SpecialBlockPrefs()) }

    OnboardingSpecialBlocksScreen(
        prefs = prefs,
        onToggleMusic = { prefs = prefs.copy(musicEnabled = !prefs.musicEnabled) },
        onToggleSteps = { prefs = prefs.copy(stepsEnabled = !prefs.stepsEnabled) },
        onToggleSleep = { prefs = prefs.copy(sleepEnabled = !prefs.sleepEnabled) },
        onToggleMenstruation = { prefs = prefs.copy(menstruationEnabled = !prefs.menstruationEnabled) },
        onSelectAll = { prefs = SpecialBlockPrefs(true, true, true, true) },
        onDeselectAll = { prefs = SpecialBlockPrefs(false, false, false, false) },
        onNavigateBack = onNavigateBack,
        onNext = {
            viewModel.saveSpecialBlocks(
                music = prefs.musicEnabled,
                sleep = prefs.sleepEnabled,
                steps = prefs.stepsEnabled,
                menstruation = prefs.menstruationEnabled
            )
            onNext()
        }
    )
}

/**
 * Stateless Component
 */
@Composable
fun OnboardingSpecialBlocksScreen(
    prefs: SpecialBlockPrefs,
    onToggleMusic: () -> Unit,
    onToggleSteps: () -> Unit,
    onToggleSleep: () -> Unit,
    onToggleMenstruation: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onNavigateBack: () -> Unit,
    onNext: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isAllSelected = prefs.musicEnabled && prefs.stepsEnabled && prefs.sleepEnabled && prefs.menstruationEnabled

    // Step 4 of 5 → 0.8f
    val progressAnim by animateFloatAsState(
        targetValue = 0.8f,
        animationSpec = tween(600),
        label = "progress"
    )

    Scaffold(
        containerColor = colorScheme.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.next),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── Top bar ──────────────────────────────────────────────────────
            item {
                OnboardingTopBar(
                    currentStep = 4,
                    onNavigateBack = onNavigateBack
                )
            }

            // ── Header ────────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.add_special_blocks_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.add_special_blocks_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {},
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.what_am_i_choosing),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.primary,
                            )
                        }

                        TextButton(
                            onClick = {
                                if (isAllSelected) onDeselectAll() else onSelectAll()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = if (isAllSelected) stringResource(R.string.deselect_all) else stringResource(R.string.select_all),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ── Blocks (Music, Steps, Sleep, Menstruation) ──────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpecialBlockCard(
                        config = SpecialBlockConfig.Music,
                        isSelected = prefs.musicEnabled,
                        onClick = onToggleMusic,
                        colorScheme = colorScheme
                    )
                    SpecialBlockCard(
                        config = SpecialBlockConfig.Steps,
                        isSelected = prefs.stepsEnabled,
                        onClick = onToggleSteps,
                        colorScheme = colorScheme
                    )
                    SpecialBlockCard(
                        config = SpecialBlockConfig.Sleep,
                        isSelected = prefs.sleepEnabled,
                        onClick = onToggleSleep,
                        colorScheme = colorScheme
                    )
                    SpecialBlockCard(
                        config = SpecialBlockConfig.Menstruation,
                        isSelected = prefs.menstruationEnabled,
                        onClick = onToggleMenstruation,
                        colorScheme = colorScheme
                    )
                }
            }
        }
    }
}

// ── Special Block Card ─────────────────────────────────────────────────────────

@Composable
private fun SpecialBlockCard(
    config: SpecialBlockConfig,
    isSelected: Boolean,
    onClick: () -> Unit,
    colorScheme: ColorScheme
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colorScheme.primary else colorScheme.onBackground.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Render the actual section
        Box(modifier = Modifier.fillMaxWidth()) {
            when (config) {
                SpecialBlockConfig.Music -> {
                    com.diary.moonpage.ui.screens.calendar.DailyMusicSection(
                        musicTitle = "Orange \"Your Lie in April\" - Piano",
                        artistName = "Marthes",
                        albumArtUrl = com.diary.moonpage.R.mipmap.ic_launcher,
                        isLinked = true,
                        showActionLink = false,
                        recentTracks = emptyList(),
                        onMusicClick = {},
                        onLinkAccount = {},
                        onTrackSelected = {}
                    )
                }
                SpecialBlockConfig.Sleep -> {
                    com.diary.moonpage.ui.screens.calendar.DailySleepSection(
                        sleepHours = 7f,
                        bedTime = java.time.LocalTime.of(0, 0),
                        wakeTime = java.time.LocalTime.of(7, 0),
                        isImporting = false,
                        showActionLink = false,
                        onSleepClick = {},
                        onImportClick = {}
                    )
                }
                SpecialBlockConfig.Steps -> {
                    com.diary.moonpage.ui.screens.calendar.DailyHealthSection(
                        steps = 2387,
                        calories = 95,
                        distance = 1.8,
                        isImporting = false,
                        showActionLink = false,
                        onImportClick = {}
                    )
                }
                SpecialBlockConfig.Menstruation -> {
                    com.diary.moonpage.ui.screens.calendar.DailyMenstruationSection(
                        isMenstruation = false,
                        onToggle = {},
                        onMenstrualClick = {}
                    )
                }
            }
        }

        // Overlay to catch clicks and show selection
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            // Tick icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(24.dp)
                    .background(if (isSelected) colorScheme.primary else Color.Transparent, CircleShape)
                    .border(
                        width = if (isSelected) 0.dp else 2.dp,
                        color = if (isSelected) Color.Transparent else colorScheme.onBackground.copy(alpha = 0.25f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
