package com.diary.moonpage.ui.screens.store

import androidx.annotation.StringRes
import com.diary.moonpage.domain.model.Theme

data class StoreUiState(
    val isLoading: Boolean = false,
    val themes: List<Theme> = emptyList(),
    val ownedThemes: List<Theme> = emptyList(),
    val userCoins: Int = 0,
    val selectedTabIndex: Int = 0, // 0 for Home, 1 for My Theme
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
    val showRecoverySuccessDialog: Boolean = false,
    @StringRes val recoveryMessageRes: Int? = null
)
