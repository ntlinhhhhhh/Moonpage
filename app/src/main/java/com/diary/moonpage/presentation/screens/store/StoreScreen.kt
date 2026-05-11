package com.diary.moonpage.presentation.screens.store

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.diary.moonpage.presentation.components.core.feedback.MoonSnackbarHost
import com.diary.moonpage.presentation.screens.store.components.*
import com.diary.moonpage.core.theme.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StoreScreen(
    viewModel: StoreViewModel,
    mainViewModel: com.diary.moonpage.MainViewModel = hiltViewModel(),
    onNavigateToDetail: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is StoreUiEffect.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is StoreUiEffect.ThemeActivated -> {
                    snackbarHostState.showSnackbar("Theme updated successfully!")
                }
                is StoreUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                StoreTopBar(
                    coins = uiState.userCoins,
                    onDoneClick = if (uiState.temporarySelectedThemeId != null) {
                        { viewModel.applyTheme() }
                    } else {
                        onNavigateBack
                    },
                    isActivate = uiState.temporarySelectedThemeId != null
                )

                StoreTabs(
                    selectedIndex = uiState.selectedTabIndex,
                    onTabSelected = { viewModel.onTabSelected(it) }
                )

                AnimatedContent(
                    targetState = uiState.selectedTabIndex,
                    transitionSpec = {
                        val slideSpec = tween<IntOffset>(300)
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = slideSpec) { it } + fadeIn()).togetherWith(
                                slideOutHorizontally(animationSpec = slideSpec) { -it } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally(animationSpec = slideSpec) { -it } + fadeIn()).togetherWith(
                                slideOutHorizontally(animationSpec = slideSpec) { it } + fadeOut()
                            )
                        }.using(SizeTransform(clip = false))
                    },
                    label = "TabAnimation"
                ) { targetIndex ->
                    when (targetIndex) {
                        0 -> HomeTabContent(
                            themes = uiState.themes,
                            selectedCategory = uiState.selectedCategory,
                            onCategoryClick = { viewModel.onCategorySelected(it) },
                            onThemeClick = {
                                viewModel.selectTheme(it)
                                onNavigateToDetail()
                            },
                            onViewAllClick = { viewModel.onTabSelected(2) }
                        )
                        1 -> MyThemeTabContent(
                            ownedThemes = uiState.ownedThemes,
                            temporarySelectedId = uiState.temporarySelectedThemeId,
                            onThemeClick = { theme ->
                                viewModel.selectTheme(theme)
                                onNavigateToDetail()
                            },
                            onExploreMore = { viewModel.onTabSelected(0) }
                        )
                        2 -> CollectionsTabContent(
                            themes = uiState.themes,
                            onThemeClick = {
                                viewModel.selectTheme(it)
                                onNavigateToDetail()
                            }
                        )
                    }
                }
            }

            // Snackbar at top
            MoonSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.TopCenter),
                topPadding = 45.dp
            )
            
            if (uiState.showConfirmActivationDialog) {
                val tempTheme = remember(uiState.temporarySelectedThemeId, uiState.ownedThemes) {
                    uiState.ownedThemes.find { it.id == uiState.temporarySelectedThemeId }
                }
                val tempThemePrimaryColor = remember(tempTheme) {
                    tempTheme?.let { getThemeShades(it).lastOrNull() }
                }
                ConfirmActivationDialog(
                    themeName = tempTheme?.name ?: "this theme",
                    onConfirm = { viewModel.confirmActivation() },
                    onCancel = { viewModel.cancelActivation() },
                    primaryColor = tempThemePrimaryColor
                )
            }
            
            if (uiState.showPurchaseSuccessDialog && uiState.purchasedTheme != null) {
                PurchaseSuccessDialog(
                    themeName = uiState.purchasedTheme?.name ?: "",
                    onDismiss = { viewModel.dismissDialog() }
                )
            }
        }
    }
}
@Composable
fun StoreTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        TabItem("Home", selectedIndex == 0) { onTabSelected(0) }
        Spacer(modifier = Modifier.width(16.dp))
        TabItem("My Theme", selectedIndex == 1) { onTabSelected(1) }
        Spacer(modifier = Modifier.width(16.dp))
        TabItem("Collections", selectedIndex == 2) { onTabSelected(2) }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = color
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
fun HomeTabContent(
    themes: List<Theme>,
    selectedCategory: String,
    onCategoryClick: (String) -> Unit,
    onThemeClick: (Theme) -> Unit,
    onViewAllClick: () -> Unit
) {
    val filteredThemes = remember(themes, selectedCategory) {
        when (selectedCategory) {
            "ALL" -> themes.filter { it.type == ThemeType.THEME }
            "LIGHT", "DARK" -> themes.filter { it.type == ThemeType.THEME }
            else -> themes.filter { it.type == ThemeType.THEME && it.category == selectedCategory }
        }
    }
    val iconPacks = themes.filter { it.type == ThemeType.ICON_PACK }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { MoonFilterChip("All Themes", selectedCategory == "ALL") { onCategoryClick("ALL") } }
                item { MoonFilterChip("Light Mode", selectedCategory == "LIGHT") { onCategoryClick("LIGHT") } }
                item { MoonFilterChip("Dark Mode", selectedCategory == "DARK") { onCategoryClick("DARK") } }
                item { MoonFilterChip("Exclusive", selectedCategory == "EXCLUSIVE") { onCategoryClick("EXCLUSIVE") } }
                item { MoonFilterChip("Newest", selectedCategory == "NEWEST") { onCategoryClick("NEWEST") } }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured Collections",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onViewAllClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        items(filteredThemes) { theme ->
            ThemeCard(theme = theme, onClick = { onThemeClick(theme) })
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Icon Collections",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onViewAllClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        items(iconPacks.chunked(2)) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { pack ->
                    Box(modifier = Modifier.weight(1f)) {
                        IconPackCard(pack) { onThemeClick(pack) }
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MyThemeTabContent(
    ownedThemes: List<Theme>,
    temporarySelectedId: String?,
    onThemeClick: (Theme) -> Unit,
    onExploreMore: () -> Unit
) {
    val currentTheme = ownedThemes.find { it.isActive }
    val otherThemes = ownedThemes.filter { !it.isActive }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentTheme != null) {
            item {
                Text(
                    text = "CURRENT THEME",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                CurrentThemeCard(currentTheme)
            }
        }

        items(otherThemes) { theme ->
            ThemeCard(
                theme = theme, 
                isSelected = theme.id == temporarySelectedId,
                showSelectionIndicator = false,
                onClick = { onThemeClick(theme) }
            )
        }

        item {
            ExploreMoreCard(onClick = onExploreMore)
        }
    }
}

@Composable
fun CollectionsTabContent(
    themes: List<Theme>,
    onThemeClick: (Theme) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "All Collections",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(themes) { theme ->
            ThemeCard(theme = theme, onClick = { onThemeClick(theme) })
        }
    }
}

@Composable
fun MoonFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
