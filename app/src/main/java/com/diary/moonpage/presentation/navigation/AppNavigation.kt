package com.diary.moonpage.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.diary.moonpage.presentation.screens.calendar.calendarScreen
import com.diary.moonpage.presentation.screens.moment.MomentCameraScreen
import com.diary.moonpage.presentation.screens.moment.MomentDetailScreen
import com.diary.moonpage.presentation.screens.profile.*
import com.diary.moonpage.presentation.screens.stats.StatisticsScreen
import com.diary.moonpage.presentation.screens.store.StoreScreen
import com.diary.moonpage.presentation.screens.store.StoreViewModel
import com.diary.moonpage.presentation.screens.store.ThemeDetailScreen
import com.diary.moonpage.presentation.tutorial.TutorialOverlay
import com.diary.moonpage.presentation.tutorial.TutorialStep
import com.diary.moonpage.presentation.tutorial.TutorialViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val authViewModel: AuthViewModel = hiltViewModel()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val activityCategoryViewModel: ActivityCategoryViewModel = hiltViewModel()
    val tutorialViewModel: TutorialViewModel = hiltViewModel()
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.Calendar.route) {
            tutorialViewModel.refresh()
        }
    }

    val mainAppRoutes = listOf(
        Screen.Calendar.route,
        Screen.Stats.route,
        Screen.Camera.route,
        Screen.Store.route,
        Screen.Profile.route
    )
    val showBottomBar = currentRoute in mainAppRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MoonBottomNavBar(
                    selectedRoute = currentRoute ?: Screen.Calendar.route,
                    onItemSelected = { route ->
                        if (currentRoute != route || route == Screen.Camera.route) {
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
        }
    ) { paddingValues ->
        @Composable
        fun ScreenWrapper(route: String, content: @Composable () -> Unit) {
            val isMainRoute = route in mainAppRoutes
            // Screens that handle their own status bar padding for a more custom layout
            val isEdgeToEdge = route == Screen.Store.route || 
                               route == Screen.ThemeDetail.route || 
                               route.startsWith("daily_log_screen")

            Box(
                modifier = Modifier
                    .then(if (!isEdgeToEdge) Modifier.statusBarsPadding() else Modifier)
                    .padding(
                        bottom = if (isMainRoute) paddingValues.calculateBottomPadding() else 0.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
                    )
            ) {
                content()
            }
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Loading.route,
            modifier = Modifier,
            enterTransition = {
                fadeIn(animationSpec = tween(400)) + slideInHorizontally(
                    initialOffsetX = { 300 },
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(400)) + slideOutHorizontally(
                    targetOffsetX = { -300 },
                    animationSpec = tween(400)
                )
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
                ScreenWrapper(Screen.Loading.route) {
                    LoadingScreen(
                        onFinished = { isLoggedIn, needsOnboarding ->
                            val nextDestination = when {
                                !isLoggedIn      -> Screen.Landing.route
                                needsOnboarding  -> Screen.OnboardingBirthday.route
                                else             -> Screen.Calendar.route
                            }
                            navController.navigate(nextDestination) {
                                popUpTo(Screen.Loading.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(Screen.Landing.route) {
                ScreenWrapper(Screen.Landing.route) {
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
                        navController.navigate(Screen.Calendar.route) {
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
                ScreenWrapper(Screen.OnboardingBirthday.route) {
                    OnboardingBirthdayScreen(
                        viewModel = onboardingViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNext = { navController.navigate(Screen.OnboardingGender.route) }
                    )
                }
            }

            composable(Screen.OnboardingGender.route) {
                ScreenWrapper(Screen.OnboardingGender.route) {
                    OnboardingGenderScreen(
                        viewModel = onboardingViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onFinish = {
                            navController.navigate(Screen.ActivityCategorySelection.route)
                        }
                    )
                }
            }

            composable(Screen.ActivityCategorySelection.route) {
                ScreenWrapper(Screen.ActivityCategorySelection.route) {
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

            calendarScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToDailyLog = { dateStr -> navController.navigate("daily_log_screen/$dateStr") },
                onNavigateToThemeCalendar = { navController.navigate(Screen.ThemeCalendar.route) },
                tutorialStep = tutorialState.step.takeIf { tutorialState.isVisible && it == TutorialStep.PickToday },
                onTutorialNext = { tutorialViewModel.next() },
                onSkipTutorial = { tutorialViewModel.complete() }
            )

            composable(Screen.Filter.route) {
                ScreenWrapper(Screen.Filter.route) {
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
                
                ScreenWrapper(Screen.DailyLog.route) {
                    DailyLogScreen(
                        dateString = dateStr,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToMusic = { navController.navigate(Screen.Music.route) },
                        onNavigateToMenstrualCycle = { navController.navigate(Screen.MenstrualCycle.route) },
                        onNavigateToDailyPhoto = { navController.navigate(Screen.DailyPhoto.route) },
                        onDone = { message ->
                            if (tutorialState.isVisible && tutorialState.step in setOf(
                                    TutorialStep.DailyActivities,
                                    TutorialStep.DailySleep,
                                    TutorialStep.DailyNote,
                                    TutorialStep.DailyPhoto
                                )
                            ) {
                                while (tutorialViewModel.state.value.step != TutorialStep.Stats) {
                                    tutorialViewModel.next()
                                }
                            }
                            navController.previousBackStackEntry?.savedStateHandle?.apply {
                                set("created_log_date", dateStr)
                                set("logSavedMessage", message)
                            }
                            navController.popBackStack()
                        },
                        tutorialStep = tutorialState.step.takeIf {
                            tutorialState.isVisible && it in setOf(
                                TutorialStep.DailyActivities,
                                TutorialStep.DailySleep,
                                TutorialStep.DailyNote,
                                TutorialStep.DailyPhoto
                            )
                        },
                        onTutorialNext = { tutorialViewModel.next() },
                        onSkipTutorial = { tutorialViewModel.complete() }
                    )
                    
                    // Trigger the VM update when a song is returned from MusicScreen
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
                ScreenWrapper(Screen.Music.route) {
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
                ScreenWrapper(Screen.MenstrualCycle.route) {
                    MenstrualCycleScreen(onNavigateBack = { navController.popBackStack() })
                }
            }

            composable(Screen.DailyPhoto.route) {
                ScreenWrapper(Screen.DailyPhoto.route) {
                    DailyPhotoScreen(onNavigateBack = { navController.popBackStack() })
                }
            }

            composable(Screen.Stats.route) {
                ScreenWrapper(Screen.Stats.route) {
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
                ScreenWrapper(Screen.Camera.route) {
                    MomentCameraScreen(
                        initialMomentId = momentId,
                        onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                        onNavigateToHistory = { /* TODO */ }
                    )
                }
            }

            composable(Screen.Store.route) { backStackEntry ->
                val storeViewModel: StoreViewModel = hiltViewModel(backStackEntry)
                ScreenWrapper(Screen.Store.route) {
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

                ScreenWrapper(Screen.ThemeDetail.route) {
                    ThemeDetailScreen(
                        viewModel = storeViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Profile.route) {
                ScreenWrapper(Screen.Profile.route) {
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
                ScreenWrapper(Screen.Account.route) {
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

            composable(Screen.Settings.route) {
                ScreenWrapper(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Notifications.route) {
                ScreenWrapper(Screen.Notifications.route) {
                    NotificationsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Widgets.route) {
                ScreenWrapper(Screen.Widgets.route) {
                    WidgetsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.InviteFriend.route) {
                ScreenWrapper(Screen.InviteFriend.route) {
                    InviteFriendScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Photos.route) {
                ScreenWrapper(Screen.Photos.route) {
                    ChangeProfilePictureScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onApply = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Gallery.route) {
                ScreenWrapper(Screen.Gallery.route) {
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
                ScreenWrapper(Screen.MomentDetail.route) {
                    MomentDetailScreen(
                        momentId = momentId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToGallery = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.ThemeCalendar.route) {
                ScreenWrapper(Screen.ThemeCalendar.route) {
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

        if (tutorialState.isVisible && currentRoute == Screen.Calendar.route && tutorialState.step in setOf(
                TutorialStep.Stats,
                TutorialStep.Camera,
                TutorialStep.Store,
                TutorialStep.Profile
            )
        ) {
            TutorialOverlay(
                step = tutorialState.step,
                onSkipStep = { tutorialViewModel.next() },
                onSkipTutorial = { tutorialViewModel.complete() }
            )
        }
    }
}
