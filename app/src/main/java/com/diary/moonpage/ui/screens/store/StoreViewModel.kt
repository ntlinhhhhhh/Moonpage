package com.diary.moonpage.ui.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.ThemeConstants
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.diary.moonpage.domain.usecase.theme.BuyThemeUseCase
import com.diary.moonpage.domain.usecase.theme.GetOwnedThemesUseCase
import com.diary.moonpage.domain.usecase.theme.GetThemesUseCase
import com.diary.moonpage.domain.usecase.theme.SetActiveThemeUseCase
import com.diary.moonpage.core.theme.MoonThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val getThemesUseCase: GetThemesUseCase,
    private val getOwnedThemesUseCase: GetOwnedThemesUseCase,
    private val buyThemeUseCase: BuyThemeUseCase,
    private val setActiveThemeUseCase: SetActiveThemeUseCase,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val themeRepository: com.diary.moonpage.domain.repository.ThemeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<StoreUiEffect>(extraBufferCapacity = 10)
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadData()
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                _uiState.update { it.copy(
                    userCoins = user?.coinBalance ?: 0,
                    streakFreezeCount = user?.streakFreezeCount ?: 0
                ) }
            }
        }
        
        viewModelScope.launch {
            themeRepository.ownedThemes.collect { owned ->
                _uiState.update { it.copy(ownedThemes = owned) }
            }
        }

        viewModelScope.launch {
            themeRepository.allThemes.collect { all ->
                _uiState.update { it.copy(themes = all) }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectTheme(theme: Theme) {
        _uiState.update { it.copy(selectedThemeDetail = theme) }
    }

    fun activateTheme(themeId: String) {
        _uiState.update { it.copy(showConfirmActivationDialog = true, temporarySelectedThemeId = themeId) }
    }


    fun selectThemeTemporarily(themeId: String) {
        _uiState.update { it.copy(temporarySelectedThemeId = themeId) }
    }

    fun applyTheme() {
        _uiState.value.temporarySelectedThemeId?.let { themeId ->
            activateTheme(themeId)
        }
    }

    fun initiatePurchase(theme: Theme) {
        onEvent(StoreUiEvent.InitiatePurchase(theme))
    }

    fun buyTheme(theme: Theme) {
        onEvent(StoreUiEvent.BuyTheme(theme))
    }

    fun cancelPurchase() {
        onEvent(StoreUiEvent.CancelPurchase)
    }

    fun dismissDialog() {
        onEvent(StoreUiEvent.DismissDialog)
    }

    fun confirmActivation() {
        onEvent(StoreUiEvent.ConfirmActivation)
    }

    fun cancelActivation() {
        onEvent(StoreUiEvent.CancelActivation)
    }

    fun onEvent(event: StoreUiEvent) {
        when (event) {
            StoreUiEvent.LoadData -> loadData()
            is StoreUiEvent.OnTabSelected -> {
                _uiState.update { it.copy(selectedTabIndex = event.index) }
            }
            is StoreUiEvent.SelectTheme -> {
                _uiState.update { it.copy(selectedThemeDetail = event.theme) }
            }
            is StoreUiEvent.InitiatePurchase -> {
                _uiState.update { it.copy(showConfirmPurchaseDialog = true, themeToPurchase = event.theme) }
            }
            StoreUiEvent.CancelPurchase -> {
                _uiState.update { it.copy(showConfirmPurchaseDialog = false, themeToPurchase = null) }
            }
            is StoreUiEvent.BuyTheme -> performBuyTheme(event.theme)
            is StoreUiEvent.ActivateTheme -> {
                _uiState.update { it.copy(showConfirmActivationDialog = true, temporarySelectedThemeId = event.themeId) }
            }
            StoreUiEvent.ConfirmActivation -> {
                val themeId = _uiState.value.temporarySelectedThemeId
                _uiState.update { it.copy(showConfirmActivationDialog = false, temporarySelectedThemeId = null) }
                themeId?.let { performActivateTheme(it) }
            }
            StoreUiEvent.CancelActivation -> {
                _uiState.update { it.copy(showConfirmActivationDialog = false, temporarySelectedThemeId = null) }
            }
            StoreUiEvent.DismissDialog -> {
                _uiState.update { it.copy(
                    showPurchaseSuccessDialog = false, 
                    showConfirmActivationDialog = false, 
                    activationSuccess = false, 
                    showConfirmFreezePurchaseDialog = false, 
                    freezePurchaseSuccess = false,
                    showRecoverySuccessDialog = false
                ) }
            }
            StoreUiEvent.InitiateFreezePurchase -> {
                _uiState.update { it.copy(showConfirmFreezePurchaseDialog = true) }
            }
            StoreUiEvent.BuyStreakFreeze -> performBuyStreakFreeze()
            StoreUiEvent.CancelFreezePurchase -> {
                _uiState.update { it.copy(showConfirmFreezePurchaseDialog = false) }
            }
            StoreUiEvent.RecoverStreak -> performRecoverStreak()
        }
    }

    private fun performRecoverStreak() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.recoverStreak().onSuccess {
                _uiState.update { it.copy(
                    isLoading = false, 
                    showRecoverySuccessDialog = true,
                    recoveryMessageRes = R.string.store_streak_recovered_success
                ) }
                _uiEffect.emit(StoreUiEffect.RecoverSuccess)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.emit(StoreUiEffect.ShowSnackBar(error.message ?: "Recovery failed"))
            }
        }
    }

    private fun performBuyStreakFreeze() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmFreezePurchaseDialog = false) }
            userRepository.buyStreakFreeze().onSuccess {
                _uiState.update { it.copy(isLoading = false, freezePurchaseSuccess = true) }
                _uiEffect.emit(StoreUiEffect.PurchaseSuccess)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.emit(StoreUiEffect.ShowSnackBar(error.message ?: "Purchase failed"))
            }
        }
    }

    fun dismissSuccessMessage() {
        _uiState.update { it.copy(activationSuccess = false) }
    }

    private fun loadData() {
        viewModelScope.launch {
            // Only show loading if we have no themes yet
            if (_uiState.value.themes.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            // 1. Fetch User Profile for current Coins
            userRepository.getCurrentUser()

            // 2. Fetch All Themes for Store (Background refresh)
            getThemesUseCase().onFailure { error ->
                if (_uiState.value.themes.isEmpty()) {
                    _uiEffect.emit(StoreUiEffect.ShowSnackBar(error.message ?: "Failed to load store themes"))
                }
            }

            // 3. Fetch Owned Themes (Background refresh)
            getOwnedThemesUseCase().onFailure { error ->
                if (_uiState.value.ownedThemes.isEmpty()) {
                    _uiEffect.emit(StoreUiEffect.ShowSnackBar(error.message ?: "Failed to load owned themes"))
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun performBuyTheme(theme: Theme) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmPurchaseDialog = false) }
            
            buyThemeUseCase(theme.id).onSuccess {
                _uiState.update { state ->
                    val updatedThemes = state.themes.map { 
                        if (it.id == theme.id) it.copy(isOwned = true) else it 
                    }
                    val purchased = updatedThemes.find { it.id == theme.id }
                    val updatedOwned = if (purchased != null) state.ownedThemes + purchased else state.ownedThemes
                    
                    val updatedDetail = if (state.selectedThemeDetail?.id == theme.id) {
                        state.selectedThemeDetail.copy(isOwned = true)
                    } else {
                        state.selectedThemeDetail
                    }

                    state.copy(
                        isLoading = false, 
                        showPurchaseSuccessDialog = true,
                        purchasedTheme = theme,
                        themes = updatedThemes,
                        ownedThemes = updatedOwned,
                        selectedThemeDetail = updatedDetail,
                        themeToPurchase = null
                    ) 
                }
                // Refresh user for coins
                userRepository.getCurrentUser()
                
                _uiEffect.emit(StoreUiEffect.PurchaseSuccess)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.emit(StoreUiEffect.ShowSnackBar(error.message ?: "Purchase failed"))
            }
        }
    }

    private fun performActivateTheme(themeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            setActiveThemeUseCase(themeId).onSuccess {
                val theme = _uiState.value.ownedThemes.find { it.id == themeId }
                
                _uiState.update { state ->
                    val updatedOwned = state.ownedThemes.map { t ->
                        t.copy(isActive = t.id == themeId)
                    }
                    
                    val updatedDetail = state.selectedThemeDetail?.copy(
                        isActive = state.selectedThemeDetail.id == themeId
                    )

                    state.copy(
                        ownedThemes = updatedOwned, 
                        isLoading = false,
                        selectedThemeDetail = updatedDetail,
                        activationSuccess = true
                    )
                }
                
                // Emit effect immediately to trigger UI with a custom message
                val themeName = theme?.name ?: "theme"
                _uiEffect.emit(StoreUiEffect.ThemeActivated(message = "Theme \"$themeName\" has been activated successfully!"))
                
                // Sufficient delay to allow UI to show snackbar before theme change (which causes global recomposition)
                delay(600)

                // Map theme and save locally
                theme?.let {
                    themePreferencesManager.setThemeType(it.toMoonThemeType())
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.emit(StoreUiEffect.ShowSnackBar(error.message ?: "Activation failed"))
            }
        }
    }
}

// Extension to map Theme to MoonThemeType
fun Theme.toMoonThemeType(): MoonThemeType {
    if (this.id == ThemeConstants.DEFAULT_THEME_ID) return MoonThemeType.DEFAULT
    return try {
        MoonThemeType.valueOf(this.decoration.uppercase())
    } catch (e: Exception) {
        MoonThemeType.DEFAULT
    }
}
