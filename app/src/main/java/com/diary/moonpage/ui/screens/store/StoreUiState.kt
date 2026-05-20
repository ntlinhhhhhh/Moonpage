package com.diary.moonpage.ui.screens.store

import androidx.annotation.StringRes
import com.diary.moonpage.domain.model.Theme

data class StoreUiState(
    val isLoading: Boolean = false,
    val themes: List<Theme> = emptyList(),
    val ownedThemes: List<Theme> = emptyList(),
    val userCoins: Int = 0,
    val selectedTabIndex: Int = 0,
    val error: String? = null,
    val showPurchaseSuccessDialog: Boolean = false,
    val showConfirmPurchaseDialog: Boolean = false,
    val themeToPurchase: Theme? = null,
    val purchasedTheme: Theme? = null,
    val selectedThemeDetail: Theme? = null,
    val selectedCategory: String = "ALL",
    val temporarySelectedThemeId: String? = null,
    val showConfirmActivationDialog: Boolean = false,
    val activationSuccess: Boolean = false,
    val showConfirmFreezePurchaseDialog: Boolean = false,
    val freezePurchaseSuccess: Boolean = false,
    val streakFreezeCount: Int = 0,
    val currentStreak: Int = 0,
    val showRecoverySuccessDialog: Boolean = false,
    @StringRes val recoveryMessageRes: Int? = null,
    val customThemes: List<Theme> = emptyList(),
    val showConfirmCustomThemeUnlockDialog: Boolean = false,
    val showInsufficientCoinsSheet: Boolean = false
)
