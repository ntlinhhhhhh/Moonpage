package com.diary.moonpage.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.diary.moonpage.presentation.screens.auth.AuthViewModel
import com.diary.moonpage.presentation.screens.calendar.calendarScreen
import com.diary.moonpage.presentation.screens.moment.momentScreen
import com.diary.moonpage.presentation.screens.profile.profileScreen
import com.diary.moonpage.presentation.screens.stats.StatisticsScreen
import com.diary.moonpage.presentation.screens.store.StoreScreen
import com.diary.moonpage.presentation.screens.store.StoreViewModel
import com.diary.moonpage.presentation.screens.store.ThemeDetailScreen

fun NavGraphBuilder.mainNavGraph(
    navController: NavController,
    authViewModel: AuthViewModel,
    screenWrapper: @Composable (String, @Composable () -> Unit) -> Unit
) {
    navigation(
        startDestination = Screen.Calendar.route,
        route = "main_graph"
    ) {
        // Module Calendar
        calendarScreen(
            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            onNavigateToDailyLog = { date -> navController.navigate("daily_log_screen/$date") },
            onNavigateToThemeCalendar = { navController.navigate(Screen.ThemeCalendar.route) }
        )

        // Module Moment
        momentScreen(navController, screenWrapper)

        // Module Profile (bao gồm Account, Gallery, ThemeCalendar, ChangeAvatar)
        profileScreen(
            navController = navController,
            onLogout = {
                authViewModel.logout()
                navController.navigate("auth_graph") {
                    popUpTo(0) { inclusive = true }
                }
            },
            screenWrapper = screenWrapper
        )

        // Module Stats
        composable(Screen.Stats.route) {
            screenWrapper(Screen.Stats.route) {
                StatisticsScreen()
            }
        }

        // Module Store (Giữ nguyên hoặc refactor sau nếu cần)
        composable(Screen.Store.route) { backStackEntry ->
            val storeViewModel: StoreViewModel = hiltViewModel(backStackEntry)
            screenWrapper(Screen.Store.route) {
                StoreScreen(
                    viewModel = storeViewModel,
                    onNavigateToDetail = { navController.navigate(Screen.ThemeDetail.route) }
                )
            }
        }

        composable(Screen.ThemeDetail.route) {
            val storeEntry = remember(it) {
                navController.getBackStackEntry(Screen.Store.route)
            }
            val storeViewModel: StoreViewModel = hiltViewModel(storeEntry)

            screenWrapper(Screen.ThemeDetail.route) {
                ThemeDetailScreen(
                    viewModel = storeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
