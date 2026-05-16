package com.diary.moonpage.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.diary.moonpage.presentation.components.core.navigation.MoonBottomNavBar
import com.diary.moonpage.presentation.screens.auth.*
import com.diary.moonpage.presentation.screens.calendar.DailyLogScreen
import com.diary.moonpage.presentation.screens.calendar.DailyPhotoScreen
import com.diary.moonpage.presentation.screens.calendar.FilterScreen
import com.diary.moonpage.presentation.screens.calendar.MenstrualCycleScreen
import com.diary.moonpage.presentation.screens.calendar.MusicScreen
import com.diary.moonpage.presentation.screens.calendar.ShareLogScreen
import com.diary.moonpage.presentation.screens.calendar.calendarScreen
import com.diary.moonpage.presentation.screens.moment.MomentCameraScreen
import com.diary.moonpage.presentation.screens.moment.MomentDetailScreen
import com.diary.moonpage.presentation.screens.notification.NotificationCenterScreen
import com.diary.moonpage.presentation.screens.profile.*
import com.diary.moonpage.presentation.screens.stats.StatisticsScreen
import com.diary.moonpage.presentation.screens.store.StoreScreen
import com.diary.moonpage.presentation.screens.store.StoreViewModel
import com.diary.moonpage.presentation.screens.store.ThemeDetailScreen
import com.diary.moonpage.presentation.screens.security.CreatePasscodeScreen
import com.diary.moonpage.presentation.screens.security.LockScreen
import com.diary.moonpage.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val isAppLocked by mainViewModel.isAppLocked.collectAsState()

    val authViewModel: AuthViewModel = hiltViewModel()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val activityCategoryViewModel: ActivityCategoryViewModel = hiltViewModel()
    val securityViewModel: com.diary.moonpage.presentation.screens.security.SecurityViewModel = hiltViewModel()

    val mainAppRoutes = remember {
        listOf(
            Screen.Calendar.route,
            Screen.Stats.route,
            Screen.Camera.route,
            Screen.Store.route,
            Screen.Profile.route
        )
    }
    val showBottomBar = currentRoute != null && currentRoute in mainAppRoutes && !isAppLocked

    Scaffold(
        // We keep BottomBar outside AnimatedVisibility for main routes to prevent jitter
    ) { paddingValues ->
        val systemBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val barHeight = 64.dp
        val totalBottomPadding = barHeight + systemBottomPadding

        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Loading.route,
                modifier = Modifier,
                enterTransition = {
                    val target = targetState.destination.route
                    val initial = initialState.destination.route
                    val isTabSwitch = target in mainAppRoutes && initial in mainAppRoutes
                    
                    if (isTabSwitch) {
                        fadeIn(animationSpec = tween(200))
                    } else {
                        fadeIn(animationSpec = tween(400)) + slideInHorizontally(
                            initialOffsetX = { 300 },
                            animationSpec = tween(400)
                        )
                    }
                },
                exitTransition = {
                    val target = targetState.destination.route
                    val initial = initialState.destination.route
                    val isTabSwitch = target in mainAppRoutes && initial in mainAppRoutes

                    if (isTabSwitch) {
                        fadeOut(animationSpec = tween(200))
                    } else {
                        fadeOut(animationSpec = tween(400)) + slideOutHorizontally(
                            targetOffsetX = { -300 },
                            animationSpec = tween(400)
                        )
                    }
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(400)) + slideInHorizontally(
                        initialOffsetX = { -300 },
                        animationSpec = tween(400)
                    )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(400)) + slideOutHorizontally(
                        targetOffsetX = { 300 },
                        animationSpec = tween(400)
                    )
                }
            ) {
                composable(Screen.Loading.route) {
                    ScreenWrapper(Screen.Loading.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        LoadingScreen(
                            onFinished = { isLoggedIn, needsOnboarding ->
                                val isPasscodeEnabled = securityViewModel.isPasscodeEnabled.value
                                if (isPasscodeEnabled) {
                                    mainViewModel.setLocked(true)
                                }
                                
                                scope.launch {
                                    val isTutorialCompleted = authViewModel.checkTutorialCompleted()
                                    val isReminderSet = onboardingViewModel.isReminderSet()
                                    val nextDestination = when {
                                        !isTutorialCompleted -> Screen.Tutorial.route
                                        !isLoggedIn      -> Screen.Landing.route
                                        needsOnboarding  -> Screen.OnboardingBirthday.route
                                        !isReminderSet   -> Screen.OnboardingReminder.route
                                        else             -> Screen.Calendar.route
                                    }
                                    navController.navigate(nextDestination) {
                                        popUpTo(Screen.Loading.route) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                }

                composable(Screen.Tutorial.route) {
                    ScreenWrapper(Screen.Tutorial.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        TutorialScreen(
                            onFinish = {
                                navController.navigate(Screen.Landing.route) {
                                    popUpTo(Screen.Tutorial.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }

                composable(Screen.Landing.route) {
                    ScreenWrapper(Screen.Landing.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        LandingScreen(
                            onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                            onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                        )
                    }
                }

                authGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    onLoginSuccess = { _, isNewUser ->
                        if (isNewUser) {
                            navController.navigate(Screen.OnboardingBirthday.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            // Go to Loading screen first to sync resources/themes from backend
                            navController.navigate(Screen.Loading.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )

                composable(Screen.OnboardingBirthday.route) {
                    ScreenWrapper(Screen.OnboardingBirthday.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        OnboardingBirthdayScreen(
                            viewModel = onboardingViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNext = { navController.navigate(Screen.OnboardingGender.route) }
                        )
                    }
                }

                composable(Screen.OnboardingGender.route) {
                    ScreenWrapper(Screen.OnboardingGender.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        OnboardingGenderScreen(
                            viewModel = onboardingViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onFinish = {
                                navController.navigate(Screen.OnboardingReminder.route)
                            }
                        )
                    }
                }

                composable(Screen.OnboardingReminder.route) {
                    ScreenWrapper(Screen.OnboardingReminder.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        OnboardingReminderScreen(
                            viewModel = onboardingViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onFinish = {
                                scope.launch {
                                    val onboardingDone = authViewModel.checkOnboardingForCurrentUser()
                                    if (onboardingDone) {
                                        navController.navigate(Screen.Calendar.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate(Screen.ActivityCategorySelection.route)
                                    }
                                }
                            }
                        )
                    }
                }

                composable(Screen.ActivityCategorySelection.route) {
                    ScreenWrapper(Screen.ActivityCategorySelection.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        ActivityCategorySelectionScreen(
                            viewModel = activityCategoryViewModel,
                            onNext = {
                                navController.navigate(Screen.Calendar.route) { popUpTo(0) { inclusive = true } }
                            },
                            onSkip = {
                                navController.navigate(Screen.Calendar.route) { popUpTo(0) { inclusive = true } }
                            }
                        )
                    }
                }

                // Settings moved here for better graph stability
                composable(Screen.Settings.route) {
                    ScreenWrapper(Screen.Settings.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        SettingsScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToLogin = {
                                navController.navigate(Screen.Landing.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToCreatePasscode = {
                                navController.navigate(Screen.CreatePasscode.route)
                            }
                        )
                    }
                }

                calendarScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDailyLog = { dateStr -> navController.navigate("daily_log_screen/$dateStr") },
                    onNavigateToShareLog = { dateStr -> navController.navigate("share_log_screen/$dateStr") },
                    onNavigateToShareCalendar = { yearMonth -> navController.navigate("share_calendar_screen/$yearMonth") },
                    onNavigateToThemeCalendar = { navController.navigate(Screen.ThemeCalendar.route) },
                    onNavigateBack = { navController.popBackStack() }
                )

                composable(Screen.Filter.route) {
                    ScreenWrapper(Screen.Filter.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        FilterScreen(
                            onDismiss = { navController.popBackStack() },
                            onSeeResults = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.DailyLog.route) { backStackEntry ->
                    val dateStr = backStackEntry.arguments?.getString("date") ?: ""
                    val savedStateHandle = backStackEntry.savedStateHandle
                    
                    val selectedSongTitle = savedStateHandle.get<String>("selected_song_title")
                    val selectedSongArtist = savedStateHandle.get<String>("selected_song_artist")
                    val selectedSongUrl = savedStateHandle.get<String>("selected_song_url")
                    
                    ScreenWrapper(Screen.DailyLog.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        DailyLogScreen(
                            dateString = dateStr,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToMusic = { navController.navigate(Screen.Music.route) },
                            onNavigateToMenstrualCycle = { navController.navigate(Screen.MenstrualCycle.route) },
                            onNavigateToDailyPhoto = { navController.navigate(Screen.DailyPhoto.route) },
                            onNavigateToShare = { date -> navController.navigate("share_log_screen/$date") },
                            onDone = { date, message ->
                                navController.previousBackStackEntry?.savedStateHandle?.apply {
                                    set("created_log_date", date)
                                    set("logSavedMessage", message)
                                }
                                navController.popBackStack()
                            }
                        )
                        
                        val viewModel: com.diary.moonpage.presentation.screens.calendar.DailyLogViewModel = hiltViewModel()
                        LaunchedEffect(selectedSongTitle) {
                            if (selectedSongTitle != null) {
                                viewModel.onEvent(com.diary.moonpage.presentation.screens.calendar.DailyLogUiEvent.OnMusicSelected(
                                    selectedSongTitle,
                                    selectedSongArtist ?: "Unknown",
                                    selectedSongUrl
                                ))
                                savedStateHandle.remove<String>("selected_song_title")
                                savedStateHandle.remove<String>("selected_song_artist")
                                savedStateHandle.remove<String>("selected_song_url")
                            }
                        }
                    }
                }

                composable(Screen.Music.route) {
                    ScreenWrapper(Screen.Music.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        MusicScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onSongSelected = { title, artist, url ->
                                navController.previousBackStackEntry?.savedStateHandle?.apply {
                                    set("selected_song_title", title)
                                    set("selected_song_artist", artist)
                                    set("selected_song_url", url)
                                }
                            }
                        )
                    }
                }

                composable(Screen.MenstrualCycle.route) {
                    ScreenWrapper(Screen.MenstrualCycle.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        MenstrualCycleScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }

                composable(Screen.DailyPhoto.route) {
                    ScreenWrapper(Screen.DailyPhoto.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        DailyPhotoScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
                
                composable(Screen.ShareLog.route) { backStackEntry ->
                    val dateStr = backStackEntry.arguments?.getString("date") ?: ""
                    ScreenWrapper(Screen.ShareLog.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        ShareLogScreen(
                            dateString = dateStr,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.Stats.route) {
                    ScreenWrapper(Screen.Stats.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatisticsScreen()
                    }
                }

                composable(
                    route = Screen.Camera.route,
                    arguments = listOf(
                        androidx.navigation.navArgument("momentId") {
                            type = androidx.navigation.NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val momentId = backStackEntry.arguments?.getString("momentId")
                    ScreenWrapper(Screen.Camera.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        MomentCameraScreen(
                            initialMomentId = momentId,
                            onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                            onNavigateToHistory = { /* TODO */ },
                            onNavigateToAccount = { navController.navigate(Screen.Account.route) }
                        )
                    }
                }

                composable(Screen.Store.route) { backStackEntry ->
                    val storeViewModel: StoreViewModel = hiltViewModel(backStackEntry)
                    ScreenWrapper(Screen.Store.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StoreScreen(
                            viewModel = storeViewModel,
                            onNavigateToDetail = { navController.navigate(Screen.ThemeDetail.route) },
                            onNavigateBack = { 
                                navController.navigate(Screen.Calendar.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }

                composable(Screen.ThemeDetail.route) {
                    val storeEntry = remember(it) {
                        navController.getBackStackEntry(Screen.Store.route)
                    }
                    val storeViewModel: StoreViewModel = hiltViewModel(storeEntry)

                    ScreenWrapper(Screen.ThemeDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        ThemeDetailScreen(
                            viewModel = storeViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.Profile.route) {
                    ScreenWrapper(Screen.Profile.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        ProfileScreen(
                            onNavigateToAccount = { navController.navigate(Screen.Account.route) },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                            onNavigateToPhotos = { navController.navigate(Screen.Gallery.route) },
                            onNavigateToThemeCalendar = { navController.navigate(Screen.ThemeCalendar.route) },
                            onNavigateToWidgets = { navController.navigate(Screen.Widgets.route) },
                            onNavigateToInviteFriend = { navController.navigate(Screen.InviteFriend.route) },
                            onNavigateToStats = { navController.navigate(Screen.Stats.route) }
                        )
                    }
                }

                composable(Screen.Account.route) {
                    ScreenWrapper(Screen.Account.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        AccountScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onLogoutClick = {
                                authViewModel.logout()
                                navController.navigate(Screen.Landing.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToChangeAvatar = { navController.navigate(Screen.Photos.route) }
                        )
                    }
                }


                composable(Screen.CreatePasscode.route) {
                    ScreenWrapper(Screen.CreatePasscode.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        CreatePasscodeScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.Notifications.route) {
                    ScreenWrapper(Screen.Notifications.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        NotificationCenterScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.Widgets.route) {
                    ScreenWrapper(Screen.Widgets.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        WidgetsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.InviteFriend.route) {
                    ScreenWrapper(Screen.InviteFriend.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        InviteFriendScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.Photos.route) {
                    ScreenWrapper(Screen.Photos.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        ChangeProfilePictureScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onApply = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.Gallery.route) {
                    ScreenWrapper(Screen.Gallery.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        GalleryScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToMomentDetail = { momentId ->
                                navController.navigate("camera_screen?momentId=$momentId")
                            }
                        )
                    }
                }

                composable(Screen.MomentDetail.route) { backStackEntry ->
                    val momentId = backStackEntry.arguments?.getString("momentId") ?: ""
                    ScreenWrapper(Screen.MomentDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        MomentDetailScreen(
                            momentId = momentId,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToGallery = { navController.popBackStack() },
                            onNavigateToAccount = { navController.navigate(Screen.Account.route) }
                        )
                    }
                }

                composable(Screen.ThemeCalendar.route) {
                    ScreenWrapper(Screen.ThemeCalendar.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        ThemeCalendarScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onActivated = {
                                navController.navigate(Screen.Calendar.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }

            // Fixed Bottom Bar - No more AnimatedVisibility for Tab Switches
            // But we use it here to handle global show/hide (e.g. Loading -> Calendar)
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                MoonBottomNavBar(
                    selectedRoute = currentRoute ?: Screen.Calendar.route,
                    modifier = Modifier.navigationBarsPadding(),
                    onItemSelected = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }

            if (isAppLocked) {
                LockScreen(
                    onUnlockSuccess = { mainViewModel.setLocked(false) }
                )
            }
        }
    }
}

@Composable
private fun ScreenWrapper(
    route: String,
    mainAppRoutes: List<String>,
    totalBottomPadding: androidx.compose.ui.unit.Dp,
    paddingValues: PaddingValues,
    content: @Composable () -> Unit
) {
    val isMainRoute = route in mainAppRoutes
    // Screens that handle their own status bar padding for a more custom layout
    val isEdgeToEdge = route == Screen.Store.route || 
                       route == Screen.ThemeDetail.route || 
                       route.startsWith("daily_log_screen")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (!isEdgeToEdge) Modifier.statusBarsPadding() else Modifier)
            .padding(
                bottom = if (isMainRoute) totalBottomPadding else 0.dp,
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
            )
    ) {
        content()
    }
}
