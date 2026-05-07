package com.diary.moonpage.presentation.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.diary.moonpage.domain.usecase.theme.BuyThemeUseCase
import com.diary.moonpage.domain.usecase.theme.GetOwnedThemesUseCase
import com.diary.moonpage.domain.usecase.theme.GetThemesUseCase
import com.diary.moonpage.domain.usecase.theme.SetActiveThemeUseCase
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
    private val setActiveThemeUseCase: SetActiveThemeUseCase
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
            _uiState.update { it.copy(userCoins = 500) }

            val mockThemes = listOf(
                Theme(
                    id = "1",
                    name = "Blushing Bean",
                    collection = "Don't stare, they're a little shy",
                    price = 70,
                    thumbnailUrl = null,
                    backgroundUrl = null,
                    type = ThemeType.THEME,
                    description = "A set of shy but sweet bean expressions.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#FFDDE1",
                    decoration = "BLUSHING"
                ),
                Theme(
                    id = "2",
                    name = "Kitty Bean",
                    collection = "Purrfect beans for the cat lover",
                    price = 140,
                    thumbnailUrl = null,
                    backgroundUrl = null,
                    type = ThemeType.THEME,
                    description = "Adorable cat-eared beans for your daily logs.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#D1D9FF",
                    decoration = "KITTY"
                ),
                Theme(
                    id = "3",
                    name = "Sprout Bean",
                    collection = "Sprout bean or bean sprout?",
                    price = 70,
                    thumbnailUrl = null,
                    backgroundUrl = null,
                    type = ThemeType.THEME,
                    description = "Tiny sprouts growing on happy little beans.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#C2E5A0",
                    decoration = "SPROUT"
                ),
                Theme(
                    id = "4",
                    name = "Midnight Light",
                    collection = "Purrfect for the night owls",
                    price = 160,
                    thumbnailUrl = null,
                    backgroundUrl = null,
                    type = ThemeType.THEME,
                    description = "Yellow moon-like beans on a deep indigo sky.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#1A1B26", 
                    decoration = "MOON"
                ),
                Theme(
                    id = "6",
                    name = "Gray Brown",
                    collection = "Earth tones Collection",
                    price = 90,
                    thumbnailUrl = null,
                    backgroundUrl = null,
                    type = ThemeType.ICON_PACK,
                    description = "A minimalist gray-brown palette for your journal.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    decoration = "NONE"
                ),
                Theme(
                    id = "7",
                    name = "Cookie Batch",
                    collection = "Sweet treats",
                    price = 80,
                    thumbnailUrl = null,
                    backgroundUrl = null,
                    type = ThemeType.ICON_PACK,
                    description = "Delicious cookies for a sweet journaling session.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    decoration = "COOKIE"
                ),
                Theme(
                    id = "8",
                    name = "Heart Felt",
                    collection = "Love is in the air",
                    price = 100,
                    thumbnailUrl = null,
                    backgroundUrl = null,
                    type = ThemeType.ICON_PACK,
                    description = "Express your feelings with these heart shapes.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    decoration = "HEART"
                ),
                Theme(
                    id = "11",
                    name = "Weather Cycle",
                    collection = "Sun to Rain",
                    price = 110,
                    thumbnailUrl = null,
                    backgroundUrl = null,
                    type = ThemeType.ICON_PACK,
                    description = "From sunny smiles to rainy tears.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    decoration = "WEATHER"
                )
            )

            val mockOwnedThemes = listOf(
                Theme(
                    id = "9",
                    name = "Default Bean",
                    collection = "Original classic",
                    price = 0,
                    thumbnailUrl = null,
                    backgroundUrl = null,
                    isActive = true,
                    isOwned = true,
                    type = ThemeType.THEME,
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#FFFBF4"
                )
            )

            _uiState.update { it.copy(
                themes = mockThemes,
                ownedThemes = mockOwnedThemes,
                isLoading = false
            ) }
        }
    }

    private fun performBuyTheme(theme: Theme) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmPurchaseDialog = false) }
            kotlinx.coroutines.delay(800)
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
                    userCoins = state.userCoins - theme.price,
                    themes = updatedThemes,
                    ownedThemes = updatedOwned,
                    selectedThemeDetail = updatedDetail,
                    themeToPurchase = null
                ) 
            }
            _uiEffect.send(StoreUiEffect.PurchaseSuccess)
        }
    }

    private fun performActivateTheme(themeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(400)
            
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
            _uiEffect.send(StoreUiEffect.ThemeActivated)
            _uiEffect.send(StoreUiEffect.ShowSnackBar("Theme activated!"))
        }
    }
}
