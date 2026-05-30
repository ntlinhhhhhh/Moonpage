package com.diary.moonpage.ui.screens.store

import androidx.compose.animation.*
import androidx.compose.ui.graphics.lerp
import kotlin.math.abs
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Snowboarding
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.vector.ImageVector
import com.diary.moonpage.R
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.diary.moonpage.ui.components.layout.SectionTitle
import com.diary.moonpage.ui.components.feedback.GlobalSnackbarManager
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.ui.components.refresh.MoonPullToRefreshBox
import com.diary.moonpage.ui.components.layout.drawVerticalScrollbar
import com.diary.moonpage.ui.screens.store.components.*
import com.diary.moonpage.ui.screens.tutorial.tutorialTarget
import com.diary.moonpage.ui.screens.tutorial.TutorialStep
import coil.compose.AsyncImage
import java.io.File

/**
 * Stateful Component
 */
@Composable
fun StoreRoute(
    viewModel: StoreViewModel,
    initialTabIndex: Int? = null,
    onInitialTabConsumed: () -> Unit = {},
    onNavigateToDetail: () -> Unit,
    onNavigateToCustomThemeEditor: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is StoreUiEffect.ShowSnackBar -> {
                    GlobalSnackbarManager.show(effect.message, effect.type)
                }
                is StoreUiEffect.ThemeActivated -> {
                    GlobalSnackbarManager.show(
                        effect.message ?: UiText.StringResource(R.string.theme_updated_success),
                        com.diary.moonpage.ui.components.feedback.SnackbarType.SUCCESS
                    )
                }
                is StoreUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
                StoreUiEffect.NavigateToCustomThemeEditor -> {
                    onNavigateToCustomThemeEditor()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(initialTabIndex) {
        initialTabIndex?.let { tab ->
            viewModel.onTabSelected(tab)
            onInitialTabConsumed()
        }
    }

    StoreScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onTabSelected = viewModel::onTabSelected,
        onCategorySelected = viewModel::onCategorySelected,
        onThemeClick = { theme ->
            viewModel.selectTheme(theme)
            onNavigateToDetail()
        },
        onViewAllClick = { viewModel.onTabSelected(3) },
        onConfirmActivation = viewModel::confirmActivation,
        onCancelActivation = viewModel::cancelActivation,
        onDismissDialog = viewModel::dismissDialog,
        onInitiateFreezePurchase = { viewModel.onEvent(StoreUiEvent.InitiateFreezePurchase) },
        onConfirmFreezePurchase = { viewModel.onEvent(StoreUiEvent.BuyStreakFreeze) },
        onCancelFreezePurchase = { viewModel.onEvent(StoreUiEvent.CancelFreezePurchase) },
        onRecoverStreak = { viewModel.onEvent(StoreUiEvent.RecoverStreak) },
        onCreateCustomThemeClick = { viewModel.onEvent(StoreUiEvent.InitiateCustomThemeUnlock) },
        onConfirmCustomThemeUnlock = { viewModel.onEvent(StoreUiEvent.ConfirmCustomThemeUnlock) },
        onCancelCustomThemeUnlock = { viewModel.onEvent(StoreUiEvent.CancelCustomThemeUnlock) },
        onDismissInsufficientCoins = { viewModel.onEvent(StoreUiEvent.DismissInsufficientCoins) },
        onActivateCustomTheme = viewModel::activateTheme,
        onRenameCustomTheme = viewModel::showRenameCustomThemeDialog,
        onConfirmRenameCustomTheme = viewModel::renameCustomTheme,
        onDismissRenameCustomTheme = viewModel::dismissRenameCustomThemeDialog,
        onRefresh = { viewModel.onEvent(StoreUiEvent.LoadData(isManualRefresh = true)) }
    )
}

/**
 * Stateless Component
 */
@Composable
fun StoreScreen(
    uiState: StoreUiState,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (Int) -> Unit,
    onCategorySelected: (String) -> Unit,
    onThemeClick: (Theme) -> Unit,
    onViewAllClick: () -> Unit,
    onConfirmActivation: () -> Unit,
    onCancelActivation: () -> Unit,
    onDismissDialog: () -> Unit,
    onInitiateFreezePurchase: () -> Unit,
    onConfirmFreezePurchase: () -> Unit,
    onCancelFreezePurchase: () -> Unit,
    onRecoverStreak: () -> Unit,
    onCreateCustomThemeClick: () -> Unit,
    onConfirmCustomThemeUnlock: () -> Unit,
    onCancelCustomThemeUnlock: () -> Unit,
    onDismissInsufficientCoins: () -> Unit,
    onActivateCustomTheme: (String) -> Unit,
    onRenameCustomTheme: (Theme) -> Unit,
    onConfirmRenameCustomTheme: (String) -> Unit,
    onDismissRenameCustomTheme: () -> Unit,
    onRefresh: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = uiState.selectedTabIndex) { 5 }
    val scope = rememberCoroutineScope()

    // Sync VM tab state to pager when it changes externally (e.g. from top bar streak click)
    LaunchedEffect(uiState.selectedTabIndex) {
        if (pagerState.currentPage != uiState.selectedTabIndex && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(uiState.selectedTabIndex)
        }
    }

    // Sync pager scroll back to VM
    LaunchedEffect(pagerState.settledPage) {
        if (uiState.selectedTabIndex != pagerState.settledPage) {
            onTabSelected(pagerState.settledPage)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        MoonPullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = paddingValues.calculateBottomPadding(),
                    start = paddingValues.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                StoreTopBar(
                    coins = uiState.userCoins,
                    onStreakClick = { onTabSelected(4) }
                )

                StoreTabs(
                    pagerState = pagerState,
                    onTabSelected = { index ->
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                )
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top,
                    beyondViewportPageCount = 1
                ) { page ->
                    when (page) {
                        0 -> HomeTabContent(
                            isActive = pagerState.currentPage == 0,
                            themes = uiState.themes,
                            selectedCategory = uiState.selectedCategory,
                            onCategoryClick = onCategorySelected,
                            onThemeClick = onThemeClick,
                            onViewAllClick = onViewAllClick
                        )
                        1 -> MyThemeTabContent(
                            isActive = pagerState.currentPage == 1,
                            ownedThemes = uiState.ownedThemes,
                            customThemes = uiState.customThemes,
                            temporarySelectedId = uiState.temporarySelectedThemeId,
                            onThemeClick = onThemeClick,
                            onActivateCustomTheme = onActivateCustomTheme,
                            onRenameCustomTheme = onRenameCustomTheme,
                            onExploreMore = { scope.launch { pagerState.animateScrollToPage(0) } }
                        )
                        2 -> CustomThemeTabContent(
                            isActive = pagerState.currentPage == 2,
                            customThemes = uiState.customThemes,
                            onActivateCustomTheme = onActivateCustomTheme,
                            onRenameCustomTheme = onRenameCustomTheme,
                            onCreateClick = onCreateCustomThemeClick
                        )
                        3 -> CollectionsTabContent(
                            isActive = pagerState.currentPage == 3,
                            themes = uiState.themes,
                            onThemeClick = onThemeClick
                        )
                        4 -> StreakFreezeTabContent(
                            freezeCount = uiState.streakFreezeCount,
                            currentStreak = uiState.currentStreak,
                            onBuyClick = onInitiateFreezePurchase,
                            onRecoverClick = onRecoverStreak
                        )
                    }
                }
            }
            if (uiState.showConfirmActivationDialog) {
                val tempTheme = remember(uiState.temporarySelectedThemeId, uiState.ownedThemes, uiState.customThemes) {
                    (uiState.ownedThemes + uiState.customThemes).distinctBy { it.id }
                        .find { it.id == uiState.temporarySelectedThemeId }
                }
                val tempThemePrimaryColor = remember(tempTheme) {
                    tempTheme?.let { getThemeShades(it).lastOrNull() }
                }
                ConfirmActivationDialog(
                    themeName = tempTheme?.name ?: stringResource(R.string.theme_calendar_this_theme),
                    onConfirm = onConfirmActivation,
                    onCancel = onCancelActivation,
                    primaryColor = tempThemePrimaryColor
                )
            }
            
            if (uiState.showConfirmFreezePurchaseDialog) {
                ConfirmFreezePurchaseDialog(
                    onConfirm = onConfirmFreezePurchase,
                    onDismiss = onCancelFreezePurchase
                )
            }
            
            if (uiState.showPurchaseSuccessDialog && (uiState.purchasedTheme != null || uiState.freezePurchaseSuccess)) {
                PurchaseSuccessDialog(
                    themeName = if (uiState.freezePurchaseSuccess) stringResource(R.string.streak_freeze) else uiState.purchasedTheme?.name ?: "",
                    onDismiss = onDismissDialog
                )
            }

            if (uiState.showRecoverySuccessDialog) {
                RecoverySuccessDialog(
                    message = uiState.recoveryMessageRes?.let { stringResource(it) }.orEmpty(),
                    onDismiss = onDismissDialog
                )
            }

            if (uiState.showConfirmCustomThemeUnlockDialog) {
                ConfirmCustomThemeUnlockDialog(
                    onConfirm = onConfirmCustomThemeUnlock,
                    onDismiss = onCancelCustomThemeUnlock
                )
            }

            if (uiState.showInsufficientCoinsSheet) {
                InsufficientCoinsBottomSheet(onDismiss = onDismissInsufficientCoins)
            }

            uiState.themeToRename?.let { theme ->
                RenameCustomThemeDialog(
                    initialName = theme.name,
                    isSaving = uiState.isRenamingTheme,
                    onConfirm = onConfirmRenameCustomTheme,
                    onDismiss = onDismissRenameCustomTheme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsufficientCoinsBottomSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Savings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.custom_theme_not_enough_coins),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.got_it))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun RenameCustomThemeDialog(
    initialName: String,
    isSaving: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    val trimmedName = name.trim()

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = {
            Text(
                text = stringResource(R.string.custom_theme_rename),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                enabled = !isSaving,
                singleLine = true,
                label = { Text(stringResource(R.string.custom_theme_name)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving && trimmedName.isNotBlank() && trimmedName != initialName,
                onClick = { onConfirm(trimmedName) }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ConfirmCustomThemeUnlockDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = com.diary.moonpage.core.theme.MoonTheme.customColors.popupBgColor
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.custom_theme_unlock_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.custom_theme_unlock_desc, CUSTOM_THEME_SLOT_PRICE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnBgColor,
                            contentColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnTextColor
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.agree))
                    }
                }
            }
        }
    }
}

@Composable
fun RecoverySuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = com.diary.moonpage.core.theme.MoonTheme.customColors.popupBgColor
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Whatshot,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.success),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.great))
                }
            }
        }
    }
}

@Composable
fun ConfirmFreezePurchaseDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = com.diary.moonpage.core.theme.MoonTheme.customColors.popupBgColor
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Snowboarding,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.store_get_streak_freeze),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stringResource(R.string.store_streak_freeze_purchase_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnBgColor,
                            contentColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnTextColor
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.store_buy_for_200))
                    }
                }
            }
        }
    }
}

@Composable
fun FreezePurchaseItem(
    onBuyClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AcUnit,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.streak_freeze),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = stringResource(R.string.streak_freeze_store_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Button(
                onClick = onBuyClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Rounded.Savings, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.custom_theme_price, 250), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StoreTabs(
    pagerState: androidx.compose.foundation.pager.PagerState,
    onTabSelected: (Int) -> Unit
) {
    val selectedIndex = pagerState.currentPage
    val scrollPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
    
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        edgePadding = 16.dp,
        divider = {},
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                val fraction = pagerState.currentPageOffsetFraction
                val currentPage = pagerState.currentPage
                val targetPage = if (fraction < 0f) {
                    (currentPage - 1).coerceAtLeast(0)
                } else {
                    (currentPage + 1).coerceAtMost(tabPositions.lastIndex)
                }
                val absFraction = kotlin.math.abs(fraction)

                val interpolatedOffset = androidx.compose.ui.unit.lerp(
                    tabPositions[currentPage].left,
                    tabPositions[targetPage].left,
                    absFraction
                )
                val interpolatedWidth = androidx.compose.ui.unit.lerp(
                    tabPositions[currentPage].width,
                    tabPositions[targetPage].width,
                    absFraction
                )

                Box(
                    Modifier
                        .wrapContentSize(Alignment.BottomStart)
                        .offset(x = interpolatedOffset)
                        .width(interpolatedWidth)
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.5.dp))
                )
            }
        }
    ) {
        val titles = listOf(
            R.string.home,
            R.string.my_theme,
            R.string.custom_theme,
            R.string.collections,
            R.string.streak_freeze
        )
        
        val activeColor = MaterialTheme.colorScheme.primary
        val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

        titles.forEachIndexed { index, titleRes ->
            // Calculate how "active" this tab is (0.0 to 1.0)
            val alpha = (1f - kotlin.math.abs(scrollPosition - index)).coerceIn(0f, 1f)
            val textColor = androidx.compose.ui.graphics.lerp(inactiveColor, activeColor, alpha)

            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = { 
                    Text(
                        text = stringResource(titleRes),
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        }
    }
}

@Composable
fun CustomThemeTabContent(
    isActive: Boolean,
    customThemes: List<Theme>,
    onActivateCustomTheme: (String) -> Unit,
    onRenameCustomTheme: (Theme) -> Unit,
    onCreateClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()

    LaunchedEffect(isActive) {
        if (!isActive) isExpanded = false
    }

    val totalItems = customThemes.size + 1 // +1 for CreateCustomThemeCard
    val displayedThemes = if (isExpanded) customThemes else customThemes.take(5)

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .drawVerticalScrollbar(gridState)
            .animateContentSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CreateCustomThemeCard(onClick = onCreateClick)
        }

        items(
            items = displayedThemes,
            key = { it.id }
        ) { theme ->
            CustomThemeCard(
                theme = theme,
                onActivateClick = { onActivateCustomTheme(theme.id) },
                onRenameClick = { onRenameCustomTheme(theme) }
            )
        }

        if (totalItems > 6) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                ViewMoreButton(
                    text = if (isExpanded) stringResource(R.string.view_less) else stringResource(R.string.view_more),
                    onClick = { isExpanded = !isExpanded }
                )
            }
        }
    }
}

@Composable
fun CreateCustomThemeCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.logCardBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.custom_theme_create_new),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.custom_theme_price, CUSTOM_THEME_SLOT_PRICE),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CustomThemeCard(
    theme: Theme,
    onActivateClick: () -> Unit = {},
    onRenameClick: () -> Unit = {}
) {
    val gradientColors = remember(theme.backgroundUrl, theme.thumbnailUrl) {
        val raw = theme.backgroundUrl ?: theme.thumbnailUrl
        if (raw != null && raw.contains(",")) {
            raw.split(",").mapNotNull { parseThemePreviewColor(it) }
        } else null
    }

    val previewColor = remember(theme.primaryColor, theme.thumbnailUrl, theme.backgroundUrl) {
        parseThemePreviewColor(theme.primaryColor)
            ?: parseThemePreviewColor(theme.thumbnailUrl)
            ?: parseThemePreviewColor(theme.backgroundUrl)
            ?: Color(0xFFE8E1DA)
    }

    val iconColors = remember(theme.id, theme.primaryColor, theme.thumbnailUrl, theme.backgroundUrl, theme.description) {
        val shades = getThemeShades(theme)
        if (shades.size >= 5) shades.take(5) else List(5) { previewColor }
    }
    
    val previewPath = remember(theme.backgroundUrl, theme.thumbnailUrl) {
        listOf(theme.backgroundUrl, theme.thumbnailUrl).firstOrNull { candidate ->
            !candidate.isNullOrBlank() && parseThemePreviewColor(candidate) == null && !candidate.contains(",")
        }
    }
    val previewModel = previewPath

    Card(
        modifier = Modifier.fillMaxWidth().height(190.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.logCardBg)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (gradientColors != null && gradientColors.size >= 2) {
                            Modifier.background(Brush.linearGradient(gradientColors))
                        } else {
                            Modifier.background(previewColor.copy(alpha = 0.22f))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Mood icons overlay (Bug 2)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY").forEachIndexed { index, mood ->
                        CuteBeanIcon(
                            modifier = Modifier.size(20.dp),
                            emotion = mood,
                            decoration = "NONE",
                            color = iconColors.getOrElse(index) { previewColor }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onRenameClick,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.custom_theme_rename),
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                text = stringResource(R.string.custom_theme_saved),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            FilledTonalButton(
                onClick = onActivateClick,
                enabled = !theme.isActive,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (theme.isActive) stringResource(R.string.custom_theme_active_now) else stringResource(R.string.custom_theme_activate),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StreakFreezeTabContent(
    freezeCount: Int,
    currentStreak: Int,
    onBuyClick: () -> Unit,
    onRecoverClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .drawVerticalScrollbar(listState),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AcUnit,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.store_freeze_count, freezeCount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.store_freeze_count_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            SectionTitle(stringResource(R.string.theme_store))
            FreezePurchaseItem(onBuyClick = onBuyClick)
        }

        item {
            SectionTitle(stringResource(R.string.store_recover_streak))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.store_manual_recover),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            currentStreak > 0 -> stringResource(R.string.store_streak_safe_desc)
                            freezeCount > 0 -> stringResource(R.string.store_manual_recover_available_desc, freezeCount)
                            else -> stringResource(R.string.store_manual_recover_unavailable_desc)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRecoverClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = freezeCount > 0
                    ) {
                        Text(stringResource(R.string.store_use_freeze_to_recover))
                    }
                }
            }
        }

        item {
            SectionTitle(stringResource(R.string.how_it_works))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HelpInfoItem(
                    title = stringResource(R.string.store_automatic_protection),
                    description = stringResource(R.string.streak_freeze_desc),
                    icon = Icons.Rounded.Snowboarding
                )
                HelpInfoItem(
                    title = stringResource(R.string.store_manual_recovery),
                    description = stringResource(R.string.store_manual_recovery_desc),
                    icon = Icons.Rounded.Whatshot
                )
                HelpInfoItem(
                    title = stringResource(R.string.store_applying_themes),
                    description = stringResource(R.string.store_applying_themes_desc),
                    icon = Icons.Rounded.GridView
                )
                HelpInfoItem(
                    title = stringResource(R.string.store_icon_packs),
                    description = stringResource(R.string.store_icon_packs_desc),
                    icon = Icons.Rounded.AcUnit
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun HelpInfoItem(
    title: String,
    description: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
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
fun ViewMoreButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun HomeTabContent(
    isActive: Boolean,
    themes: List<Theme>,
    selectedCategory: String,
    onCategoryClick: (String) -> Unit,
    onThemeClick: (Theme) -> Unit,
    onViewAllClick: () -> Unit
) {
    var isExpanded by remember(selectedCategory) { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    LaunchedEffect(isActive) {
        if (!isActive) isExpanded = false
    }

    val allThemesInCategory = remember(themes, selectedCategory) {
        when (selectedCategory) {
            "ALL" -> themes.filter { it.type == ThemeType.THEME }
            "LIGHT", "DARK" -> themes.filter { it.type == ThemeType.THEME }
            else -> themes.filter { it.type == ThemeType.THEME && it.category == selectedCategory }
        }.filter { !it.isOwned }
    }
    
    val filteredThemes = if (isExpanded) allThemesInCategory else allThemesInCategory.take(3)
    val iconPacks = themes.filter { it.type == ThemeType.ICON_PACK }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .drawVerticalScrollbar(listState)
            .animateContentSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { MoonFilterChip(stringResource(R.string.all_themes), selectedCategory == "ALL") { onCategoryClick("ALL") } }
                item { MoonFilterChip(stringResource(R.string.light_mode), selectedCategory == "LIGHT") { onCategoryClick("LIGHT") } }
                item { MoonFilterChip(stringResource(R.string.dark_mode), selectedCategory == "DARK") { onCategoryClick("DARK") } }
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
                    text = stringResource(R.string.featured_collections),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.view_all),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onViewAllClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        itemsIndexed(
            items = filteredThemes,
            key = { _, theme -> theme.id }
        ) { index, theme ->
            val previewDarkMode = when (selectedCategory) {
                "LIGHT" -> false
                "DARK" -> true
                else -> null
            }
            Box(
                modifier = if (index == 0) Modifier.tutorialTarget(TutorialStep.HighlightStoreThemes) else Modifier
            ) {
                ThemeCard(
                    theme = theme,
                    previewDarkMode = previewDarkMode,
                    onClick = { onThemeClick(theme) }
                )
            }
        }

        if (allThemesInCategory.size > 3) {
            item {
                ViewMoreButton(
                    text = if (isExpanded) stringResource(R.string.view_less) else stringResource(R.string.view_more),
                    onClick = { isExpanded = !isExpanded }
                )
            }
        }

//        item {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = stringResource(R.string.icon_collections),
//                    color = MaterialTheme.colorScheme.onBackground,
//                    style = MaterialTheme.typography.titleLarge
//                )
//                Text(
//                    text = stringResource(R.string.view_all),
//                    style = MaterialTheme.typography.labelLarge,
//                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(8.dp))
//                        .clickable { onViewAllClick() }
//                        .padding(horizontal = 8.dp, vertical = 4.dp)
//                )
//            }
//        }

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
    isActive: Boolean,
    ownedThemes: List<Theme>,
    customThemes: List<Theme>,
    temporarySelectedId: String?,
    onThemeClick: (Theme) -> Unit,
    onActivateCustomTheme: (String) -> Unit,
    onRenameCustomTheme: (Theme) -> Unit,
    onExploreMore: () -> Unit
) {
    var otherThemesVisibleCount by remember { mutableIntStateOf(3) }
    var customThemesVisibleCount by remember { mutableIntStateOf(4) }
    val listState = rememberLazyListState()

    LaunchedEffect(isActive) {
        if (!isActive) {
            otherThemesVisibleCount = 3
            customThemesVisibleCount = 4
        }
    }

    val currentTheme = ownedThemes.find { it.isActive }
    val otherThemes = ownedThemes.filter { !it.isActive && !it.id.startsWith("custom_") }
    
    val displayedOtherThemes = otherThemes.take(otherThemesVisibleCount)
    val displayedCustomThemes = customThemes.take(customThemesVisibleCount)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .drawVerticalScrollbar(listState)
            .animateContentSize(animationSpec = tween(400)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentTheme != null) {
            item(key = "current_theme_header") {
                Text(
                    text = stringResource(R.string.current_theme),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                CurrentThemeCard(currentTheme)
            }
        }

        if (otherThemes.isNotEmpty()) {
            items(
                items = displayedOtherThemes,
                key = { it.id },
                contentType = { "theme" }
            ) { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = theme.id == temporarySelectedId,
                    showSelectionIndicator = false,
                    onClick = { onThemeClick(theme) }
                )
            }
            
            if (otherThemes.size > 3) {
                item(key = "other_themes_view_more") {
                    ViewMoreButton(
                        text = if (otherThemesVisibleCount >= otherThemes.size) stringResource(R.string.view_less) else stringResource(R.string.view_more),
                        onClick = {
                            if (otherThemesVisibleCount >= otherThemes.size) {
                                otherThemesVisibleCount = 3
                            } else {
                                otherThemesVisibleCount = (otherThemesVisibleCount + 3).coerceAtMost(otherThemes.size)
                            }
                        }
                    )
                }
            }
        }

        if (customThemes.isNotEmpty()) {
            item(key = "custom_theme_header") {
                Text(
                    text = stringResource(R.string.custom_theme),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
            items(
                items = displayedCustomThemes.chunked(2),
                key = { row -> "custom_row_" + row.joinToString { it.id } }
            ) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { theme ->
                        Box(modifier = Modifier.weight(1f)) {
                            CustomThemeCard(
                                theme = theme,
                                onActivateClick = { onActivateCustomTheme(theme.id) },
                                onRenameClick = { onRenameCustomTheme(theme) }
                            )
                        }
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            if (customThemes.size > 4) {
                item(key = "custom_themes_view_more") {
                    ViewMoreButton(
                        text = if (customThemesVisibleCount >= customThemes.size) stringResource(R.string.view_less) else stringResource(R.string.view_more),
                        onClick = {
                            if (customThemesVisibleCount >= customThemes.size) {
                                customThemesVisibleCount = 4
                            } else {
                                customThemesVisibleCount = (customThemesVisibleCount + 4).coerceAtMost(customThemes.size)
                            }
                        }
                    )
                }
            }
        }

        item {
            ExploreMoreCard(onClick = onExploreMore)
        }
    }
}

private fun parseThemePreviewColor(value: String?): Color? {
    if (value.isNullOrBlank()) return null
    val raw = value.trim()
    val normalized = when {
        raw.startsWith("#") -> raw
        raw.startsWith("0x", ignoreCase = true) -> "#${raw.drop(2)}"
        raw.length == 6 || raw.length == 8 -> "#$raw"
        else -> return null
    }
    return runCatching {
        Color(android.graphics.Color.parseColor(normalized))
    }.getOrNull()
}

@Composable
fun CollectionsTabContent(
    isActive: Boolean,
    themes: List<Theme>,
    onThemeClick: (Theme) -> Unit
) {
    val purchasedThemes = remember(themes) { themes.filter { it.isOwned } }
    val unpurchasedThemes = remember(themes) { themes.filter { !it.isOwned } }
    var isPurchasedExpanded by remember { mutableStateOf(false) }
    var isUnpurchasedExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(isActive) {
        if (!isActive) {
            isPurchasedExpanded = false
            isUnpurchasedExpanded = false
        }
    }

    val displayedPurchased = if (isPurchasedExpanded) purchasedThemes else purchasedThemes.take(3)
    val displayedUnpurchased = if (isUnpurchasedExpanded) unpurchasedThemes else unpurchasedThemes.take(3)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .drawVerticalScrollbar(listState)
            .animateContentSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (purchasedThemes.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.store_purchased),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(
                items = displayedPurchased,
                key = { it.id },
                contentType = { "theme" }
            ) { theme ->
                ThemeCard(theme = theme, onClick = { onThemeClick(theme) })
            }
            if (purchasedThemes.size > 3) {
                item {
                    ViewMoreButton(
                        text = if (isPurchasedExpanded) stringResource(R.string.view_less) else stringResource(R.string.view_more),
                        onClick = { isPurchasedExpanded = !isPurchasedExpanded }
                    )
                }
            }
        }

        if (unpurchasedThemes.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.store_unpurchased),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(
                items = displayedUnpurchased,
                key = { it.id },
                contentType = { "theme" }
            ) { theme ->
                ThemeCard(theme = theme, onClick = { onThemeClick(theme) })
            }
            if (unpurchasedThemes.size > 3) {
                item {
                    ViewMoreButton(
                        text = if (isUnpurchasedExpanded) stringResource(R.string.view_less) else stringResource(R.string.view_more),
                        onClick = { isUnpurchasedExpanded = !isUnpurchasedExpanded }
                    )
                }
            }
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


