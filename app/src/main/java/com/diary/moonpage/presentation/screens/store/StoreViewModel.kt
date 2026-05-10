package com.diary.moonpage.presentation.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.diary.moonpage.domain.usecase.theme.BuyThemeUseCase
import com.diary.moonpage.domain.usecase.theme.GetOwnedThemesUseCase
import com.diary.moonpage.domain.usecase.theme.GetThemesUseCase
import com.diary.moonpage.domain.usecase.theme.SetActiveThemeUseCase
import com.diary.moonpage.core.theme.MoonThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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
    private val tokenManager: com.diary.moonpage.core.util.TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<StoreUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadData()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun selectTheme(theme: Theme) {
        _uiState.update { it.copy(selectedThemeDetail = theme) }
    }

    fun activateTheme(themeId: String) {
        onEvent(StoreUiEvent.ActivateTheme(themeId))
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
            is StoreUiEvent.ActivateTheme -> performActivateTheme(event.themeId)
            StoreUiEvent.DismissDialog -> {
                _uiState.update { it.copy(showPurchaseSuccessDialog = false) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            tokenManager.getToken().first()?.let { token ->
                // Fetch All Themes for Store
                getThemesUseCase(token).onSuccess { themes ->
                    _uiState.update { it.copy(themes = themes) }
                }.onFailure { error ->
                    _uiEffect.send(StoreUiEffect.ShowSnackBar(error.message ?: "Failed to load store themes"))
                }

                // Fetch Owned Themes
                getOwnedThemesUseCase(token).onSuccess { owned ->
                    _uiState.update { it.copy(ownedThemes = owned) }
                }.onFailure { error ->
                    _uiEffect.send(StoreUiEffect.ShowSnackBar(error.message ?: "Failed to load owned themes"))
                }
            } ?: run {
                _uiEffect.send(StoreUiEffect.ShowSnackBar("Session expired. Please login again."))
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun performBuyTheme(theme: Theme) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmPurchaseDialog = false) }
            
            tokenManager.getToken().first()?.let { token ->
                buyThemeUseCase(token, theme.id).onSuccess {
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
                    _uiEffect.send(StoreUiEffect.PurchaseSuccess)
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(StoreUiEffect.ShowSnackBar(error.message ?: "Purchase failed"))
                }
            }
        }
    }

    private fun performActivateTheme(themeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            tokenManager.getToken().first()?.let { token ->
                setActiveThemeUseCase(token, themeId).onSuccess {
                    _uiState.update { state ->
                        val updatedOwned = state.ownedThemes.map { theme ->
                            theme.copy(isActive = theme.id == themeId)
                        }
                        
                        val updatedDetail = state.selectedThemeDetail?.copy(
                            isActive = state.selectedThemeDetail.id == themeId
                        )

                        state.copy(
                            ownedThemes = updatedOwned, 
                            isLoading = false,
                            selectedThemeDetail = updatedDetail
                        )
                    }
                    
                    // Map theme type and save locally
                    val theme = _uiState.value.ownedThemes.find { it.id == themeId }
                    theme?.let {
                        themePreferencesManager.setThemeType(it.type.toMoonThemeType())
                    }
                    
                    _uiEffect.send(StoreUiEffect.ThemeActivated)
                    _uiEffect.send(StoreUiEffect.ShowSnackBar("Theme activated!"))
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(StoreUiEffect.ShowSnackBar(error.message ?: "Activation failed"))
                }
            }
        }
    }
}

// Extension to map ThemeType to MoonThemeType
fun ThemeType.toMoonThemeType(): MoonThemeType {
    return try {
        MoonThemeType.valueOf(this.name)
    } catch (e: Exception) {
        MoonThemeType.DEFAULT
    }
}
