package com.diary.moonpage.presentation.screens.store

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
    object DismissDialog : StoreUiEvent()
}

/**
 * Effects: VM -> UI
 */
sealed class StoreUiEffect {
    data class ShowSnackBar(val message: String) : StoreUiEffect()
    object PurchaseSuccess : StoreUiEffect()
    object ThemeActivated : StoreUiEffect()
    object NavigateBack : StoreUiEffect()
}
