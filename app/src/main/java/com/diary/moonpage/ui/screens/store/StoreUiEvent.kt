package com.diary.moonpage.ui.screens.store

import com.diary.moonpage.domain.model.Theme

/**
 * Events: UI -> VM
 */
sealed class StoreUiEvent {
    object LoadData : StoreUiEvent()
    data class OnTabSelected(val index: Int) : StoreUiEvent()
    data class SelectTheme(val theme: Theme) : StoreUiEvent()
    data class InitiatePurchase(val theme: Theme) : StoreUiEvent()
    object CancelPurchase : StoreUiEvent()
    data class BuyTheme(val theme: Theme) : StoreUiEvent()
    data class ActivateTheme(val themeId: String) : StoreUiEvent()
    object ConfirmActivation : StoreUiEvent()
    object CancelActivation : StoreUiEvent()
    object DismissDialog : StoreUiEvent()
    
    // Streak Freeze
    object InitiateFreezePurchase : StoreUiEvent()
    object BuyStreakFreeze : StoreUiEvent()
    object CancelFreezePurchase : StoreUiEvent()
    object RecoverStreak : StoreUiEvent()
}

/**
 * Effects: VM -> UI
 */
sealed class StoreUiEffect {
    data class ShowSnackBar(val message: String) : StoreUiEffect()
    object PurchaseSuccess : StoreUiEffect()
    object RecoverSuccess : StoreUiEffect()
    data class ThemeActivated(val message: String? = null) : StoreUiEffect()
    object NavigateBack : StoreUiEffect()
}
