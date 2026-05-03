package com.diary.moonpage.presentation.screens.profile

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.diary.moonpage.presentation.navigation.Screen

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    this.navigate(Screen.Profile.route, navOptions)
}

fun NavController.navigateToAccount(navOptions: NavOptions? = null) {
    this.navigate(Screen.Account.route, navOptions)
}

fun NavController.navigateToGallery(navOptions: NavOptions? = null) {
    this.navigate(Screen.Gallery.route, navOptions)
}

fun NavController.navigateToThemeCalendar(navOptions: NavOptions? = null) {
    this.navigate(Screen.ThemeCalendar.route, navOptions)
}

fun NavController.navigateToChangeAvatar(navOptions: NavOptions? = null) {
    this.navigate(Screen.Photos.route, navOptions)
}

fun NavGraphBuilder.profileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    screenWrapper: @Composable (String, @Composable () -> Unit) -> Unit
) {
    composable(Screen.Profile.route) {
        screenWrapper(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToAccount = { navController.navigateToAccount() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToPhotos = { navController.navigateToGallery() },
                onNavigateToThemeCalendar = { navController.navigateToThemeCalendar() },
                onNavigateToWidgets = { navController.navigate(Screen.Widgets.route) },
                onNavigateToInviteFriend = { navController.navigate(Screen.InviteFriend.route) }
            )
        }
    }

    composable(Screen.Account.route) {
        screenWrapper(Screen.Account.route) {
            AccountScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogoutClick = onLogout,
                onNavigateToChangeAvatar = { navController.navigateToChangeAvatar() }
            )
        }
    }

    composable(Screen.Photos.route) {
        screenWrapper(Screen.Photos.route) {
            ChangeProfilePictureScreen(
                onNavigateBack = { navController.popBackStack() },
                onApply = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.Gallery.route) {
        screenWrapper(Screen.Gallery.route) {
            GalleryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMomentDetail = { id -> 
                    // This assumes moment_detail route is available
                    navController.navigate("moment_detail/$id")
                }
            )
        }
    }

    composable(Screen.ThemeCalendar.route) {
        screenWrapper(Screen.ThemeCalendar.route) {
            ThemeCalendarScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
