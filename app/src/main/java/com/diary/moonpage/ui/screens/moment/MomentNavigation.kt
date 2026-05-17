package com.diary.moonpage.ui.screens.moment

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.diary.moonpage.ui.navigation.Screen
import com.diary.moonpage.ui.screens.profile.navigateToAccount

fun NavController.navigateToMomentCamera(navOptions: NavOptions? = null) {
    this.navigate(Screen.Camera.route, navOptions)
}

fun NavController.navigateToMomentHistory(navOptions: NavOptions? = null) {
    this.navigate("moment_history", navOptions)
}

fun NavController.navigateToMomentDetail(momentId: String, navOptions: NavOptions? = null) {
    this.navigate("moment_detail/$momentId", navOptions)
}

fun NavGraphBuilder.momentScreen(
    navController: NavController,
    screenWrapper: @Composable (String, @Composable () -> Unit) -> Unit
) {
    composable(Screen.Camera.route) {
        screenWrapper(Screen.Camera.route) {
            MomentCameraRoute(
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToHistory = { navController.navigateToMomentHistory() },
                onNavigateToAccount = { navController.navigateToAccount() }
            )
        }
    }

    composable("moment_history") {
        screenWrapper("moment_history") {
            MomentHistoryRoute(
                onBackToCamera = { navController.popBackStack() },
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToDetail = { id -> navController.navigateToMomentDetail(id) },
                onNavigateToAccount = { navController.navigateToAccount() }
            )
        }
    }

    composable("moment_detail/{momentId}") { backStackEntry ->
        val momentId = backStackEntry.arguments?.getString("momentId") ?: return@composable
        screenWrapper("moment_detail") {
            MomentDetailRoute(
                momentId = momentId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToAccount = { navController.navigateToAccount() }
            )
        }
    }
}
