package com.diary.moonpage.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.diary.moonpage.ui.components.navigation.MoonBottomNavBar
import com.diary.moonpage.ui.screens.auth.*
import com.diary.moonpage.ui.screens.calendar.DailyLogRoute
import com.diary.moonpage.ui.screens.calendar.DailyPhotoRoute
import com.diary.moonpage.ui.screens.calendar.FilterRoute
import com.diary.moonpage.ui.screens.calendar.MenstrualCycleRoute
import com.diary.moonpage.ui.screens.calendar.MusicRoute
import com.diary.moonpage.ui.screens.calendar.ShareLogRoute
import com.diary.moonpage.ui.screens.calendar.calendarScreen
import com.diary.moonpage.ui.screens.moment.MomentCameraRoute
import com.diary.moonpage.ui.screens.moment.MomentDetailRoute
import com.diary.moonpage.ui.screens.notification.NotificationCenterRoute
import com.diary.moonpage.ui.screens.profile.*
import com.diary.moonpage.ui.screens.stats.StatisticsRoute
import com.diary.moonpage.ui.screens.stats.StatisticsViewModel
import com.diary.moonpage.ui.screens.stats.StatsMoodDetailRoute
import com.diary.moonpage.ui.screens.stats.StatsSleepDetailRoute
import com.diary.moonpage.ui.screens.stats.StatsActivityDetailRoute
import com.diary.moonpage.ui.screens.stats.StatsInsightsDetailRoute
import com.diary.moonpage.ui.screens.stats.StatsMusicDetailRoute
import com.diary.moonpage.ui.screens.stats.StatsAnnualMoodDetailRoute
import com.diary.moonpage.ui.screens.stats.StatsAnnualSleepDetailRoute
import com.diary.moonpage.ui.screens.stats.StatsAnnualActivityDetailRoute
import com.diary.moonpage.ui.screens.stats.StatsAnnualBeansDetailRoute
import com.diary.moonpage.ui.screens.stats.StatsAnnualMusicDetailRoute
import com.diary.moonpage.ui.screens.store.StoreRoute
import com.diary.moonpage.ui.screens.store.StoreViewModel
import com.diary.moonpage.ui.screens.store.ThemeDetailRoute
import com.diary.moonpage.ui.screens.security.CreatePasscodeRoute
import com.diary.moonpage.ui.screens.security.LockRoute
import com.diary.moonpage.ui.MainViewModel
import com.diary.moonpage.ui.tutorial.LocalTutorialController
import com.diary.moonpage.ui.tutorial.TutorialController
import com.diary.moonpage.ui.tutorial.TutorialOverlay
import com.diary.moonpage.ui.tutorial.TutorialViewModel
import com.diary.moonpage.ui.tutorial.TutorialStep
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
    val securityViewModel: com.diary.moonpage.ui.screens.security.SecurityViewModel = hiltViewModel()
    val tutorialViewModel: TutorialViewModel = hiltViewModel()
    val tutorialState by tutorialViewModel.state.collectAsState()
    var tutorialTargetBounds by remember { mutableStateOf<Map<TutorialStep, Rect>>(emptyMap()) }

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
            LaunchedEffect(currentRoute) {
                if (currentRoute == Screen.Calendar.route || currentRoute == Screen.DailyLog.route) {
                    tutorialViewModel.refresh()
                }
            }
            LaunchedEffect(currentRoute, tutorialState.step) {
                tutorialTargetBounds = emptyMap()
            }
            val tutorialController = TutorialController(
                activeStep = tutorialState.step.takeIf { tutorialState.isVisible },
                onTargetBoundsChanged = { step, bounds ->
                    tutorialTargetBounds = tutorialTargetBounds.toMutableMap().apply {
                        if (this[step] != bounds) {
                            this[step] = bounds
                        }
                    }
                },
                onStepCompleted = { step -> tutorialViewModel.completeStep(step) }
            )

            CompositionLocalProvider(LocalTutorialController provides tutorialController) {
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
                                    val isReminderSet = onboardingViewModel.isReminderSet()
                                    val nextDestination = when {
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
                        OnboardingBirthdayRoute(
                            viewModel = onboardingViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNext = { navController.navigate(Screen.OnboardingGender.route) }
                        )
                    }
                }

                composable(Screen.OnboardingGender.route) {
                    ScreenWrapper(Screen.OnboardingGender.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        OnboardingGenderRoute(
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
                        OnboardingReminderRoute(
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
                        ActivityCategorySelectionRoute(
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
                        SettingsRoute(
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
                        FilterRoute(
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
                        DailyLogRoute(
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
                        
                        val viewModel: com.diary.moonpage.ui.screens.calendar.DailyLogViewModel = hiltViewModel()
                        LaunchedEffect(selectedSongTitle) {
                            if (selectedSongTitle != null) {
                                viewModel.onEvent(com.diary.moonpage.ui.screens.calendar.DailyLogUiEvent.OnMusicSelected(
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
                        MusicRoute(
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
                        MenstrualCycleRoute(onNavigateBack = { navController.popBackStack() })
                    }
                }

                composable(Screen.DailyPhoto.route) {
                    ScreenWrapper(Screen.DailyPhoto.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        DailyPhotoRoute(onNavigateBack = { navController.popBackStack() })
                    }
                }
                
                composable(Screen.ShareLog.route) { backStackEntry ->
                    val dateStr = backStackEntry.arguments?.getString("date") ?: ""
                    ScreenWrapper(Screen.ShareLog.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        ShareLogRoute(
                            dateString = dateStr,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.Stats.route) {
                    ScreenWrapper(Screen.Stats.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatisticsRoute(
                            onNavigateToMoodDetail = { navController.navigate(Screen.StatsMoodDetail.route) },
                            onNavigateToSleepDetail = { navController.navigate(Screen.StatsSleepDetail.route) },
                            onNavigateToActivityDetail = { navController.navigate(Screen.StatsActivityDetail.route) },
                            onNavigateToInsightsDetail = { navController.navigate(Screen.StatsInsightsDetail.route) },
                            onNavigateToMusicDetail = { navController.navigate(Screen.StatsMusicDetail.route) },
                            onNavigateToAnnualMoodDetail = { navController.navigate(Screen.StatsAnnualMoodDetail.route) },
                            onNavigateToAnnualSleepDetail = { navController.navigate(Screen.StatsAnnualSleepDetail.route) },
                            onNavigateToAnnualActivityDetail = { navController.navigate(Screen.StatsAnnualActivityDetail.route) },
                            onNavigateToAnnualBeansDetail = { navController.navigate(Screen.StatsAnnualBeansDetail.route) },
                            onNavigateToAnnualMusicDetail = { navController.navigate(Screen.StatsAnnualMusicDetail.route) }
                        )
                    }
                }

                composable(Screen.StatsMoodDetail.route) { backStackEntry ->
                    val statsEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Stats.route) }
                    val viewModel: StatisticsViewModel = hiltViewModel(statsEntry)
                    ScreenWrapper(Screen.StatsMoodDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatsMoodDetailRoute(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
                    }
                }

                composable(Screen.StatsSleepDetail.route) { backStackEntry ->
                    val statsEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Stats.route) }
                    val viewModel: StatisticsViewModel = hiltViewModel(statsEntry)
                    ScreenWrapper(Screen.StatsSleepDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatsSleepDetailRoute(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
                    }
                }

                composable(Screen.StatsActivityDetail.route) { backStackEntry ->
                    val statsEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Stats.route) }
                    val viewModel: StatisticsViewModel = hiltViewModel(statsEntry)
                    ScreenWrapper(Screen.StatsActivityDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatsActivityDetailRoute(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
                    }
                }

                composable(Screen.StatsInsightsDetail.route) { backStackEntry ->
                    val statsEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Stats.route) }
                    val viewModel: StatisticsViewModel = hiltViewModel(statsEntry)
                    ScreenWrapper(Screen.StatsInsightsDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatsInsightsDetailRoute(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
                    }
                }

                composable(Screen.StatsMusicDetail.route) { backStackEntry ->
                    val statsEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Stats.route) }
                    val viewModel: StatisticsViewModel = hiltViewModel(statsEntry)
                    ScreenWrapper(Screen.StatsMusicDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatsMusicDetailRoute(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
                    }
                }
                
                // --- ANNUAL STATS DETAILS ---
                composable(Screen.StatsAnnualMoodDetail.route) { backStackEntry ->
                    val statsEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Stats.route) }
                    val viewModel: StatisticsViewModel = hiltViewModel(statsEntry)
                    ScreenWrapper(Screen.StatsAnnualMoodDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatsAnnualMoodDetailRoute(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                }
                
                composable(Screen.StatsAnnualSleepDetail.route) { backStackEntry ->
                    val statsEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Stats.route) }
                    val viewModel: StatisticsViewModel = hiltViewModel(statsEntry)
                    ScreenWrapper(Screen.StatsAnnualSleepDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatsAnnualSleepDetailRoute(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                }
                
                composable(Screen.StatsAnnualActivityDetail.route) { backStackEntry ->
                    val statsEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Stats.route) }
                    val viewModel: StatisticsViewModel = hiltViewModel(statsEntry)
                    ScreenWrapper(Screen.StatsAnnualActivityDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatsAnnualActivityDetailRoute(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                }
                
                composable(Screen.StatsAnnualBeansDetail.route) { backStackEntry ->
                    val statsEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Stats.route) }
                    val viewModel: StatisticsViewModel = hiltViewModel(statsEntry)
                    ScreenWrapper(Screen.StatsAnnualBeansDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatsAnnualBeansDetailRoute(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                }
                
                composable(Screen.StatsAnnualMusicDetail.route) { backStackEntry ->
                    val statsEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Stats.route) }
                    val viewModel: StatisticsViewModel = hiltViewModel(statsEntry)
                    ScreenWrapper(Screen.StatsAnnualMusicDetail.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StatsAnnualMusicDetailRoute(viewModel = viewModel, onBack = { navController.popBackStack() })
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
                        MomentCameraRoute(
                            initialMomentId = momentId,
                            onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                            onNavigateToHistory = { navController.navigate(Screen.Gallery.route) },
                            onNavigateToAccount = { navController.navigate(Screen.Account.route) }
                        )
                    }
                }

                composable(Screen.Store.route) { backStackEntry ->
                    val storeViewModel: StoreViewModel = hiltViewModel(backStackEntry)
                    ScreenWrapper(Screen.Store.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        StoreRoute(
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
                        ThemeDetailRoute(
                            viewModel = storeViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.Profile.route) {
                    ScreenWrapper(Screen.Profile.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        ProfileRoute(
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
                        AccountRoute(
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
                        CreatePasscodeRoute(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.Notifications.route) {
                    ScreenWrapper(Screen.Notifications.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        NotificationCenterRoute(
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
                        MomentDetailRoute(
                            momentId = momentId,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToGallery = { navController.popBackStack() },
                            onNavigateToAccount = { navController.navigate(Screen.Account.route) }
                        )
                    }
                }

                composable(Screen.ThemeCalendar.route) {
                    ScreenWrapper(Screen.ThemeCalendar.route, mainAppRoutes, totalBottomPadding, paddingValues) {
                        ThemeCalendarRoute(
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
                LockRoute(
                    onUnlockSuccess = { mainViewModel.setLocked(false) }
                )
            }

            if (
                tutorialState.isVisible &&
                (currentRoute == Screen.Calendar.route || currentRoute?.startsWith("daily_log_screen") == true)
            ) {
                TutorialOverlay(
                    step = tutorialState.step,
                    targetBounds = tutorialTargetBounds[tutorialState.step],
                    onSkipStep = {
                        if (tutorialState.step == TutorialStep.HighlightCurrentDay ||
                            tutorialState.step == TutorialStep.HighlightDoneButton
                        ) return@TutorialOverlay
                        tutorialViewModel.skipStep()
                    },
                    onSkipTutorial = { tutorialViewModel.complete() },
                    modifier = Modifier.fillMaxSize()
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
