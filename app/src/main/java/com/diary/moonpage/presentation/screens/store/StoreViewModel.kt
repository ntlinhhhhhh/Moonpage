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
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager
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
                    id = "1", name = "Blushing", collection = "Soft rosy tones", price = 1000,
                    thumbnailUrl = "#F07063", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "A set of shy but sweet rosy expressions.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#F07063", decoration = "BLUSHING"
                ),
                Theme(
                    id = "2", name = "Kitty", collection = "Purrfect for lovers", price = 1200,
                    thumbnailUrl = "#7A93FF", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Adorable cat-inspired theme for your daily logs.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#7A93FF", decoration = "KITTY"
                ),
                Theme(
                    id = "3", name = "Sprout", collection = "Fresh beginnings", price = 800,
                    thumbnailUrl = "#6EC276", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Tiny sprouts for a fresh journaling experience.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#6EC276", decoration = "SPROUT"
                ),
                Theme(
                    id = "4", name = "Midnight", collection = "Night owl style", price = 1600,
                    thumbnailUrl = "#1A1B26", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Deep indigo theme for the midnight thinkers.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#1A1B26", decoration = "MIDNIGHT"
                ),
                Theme(
                    id = "5", name = "Sunny", collection = "Hello sunshine!", price = 1000,
                    thumbnailUrl = "#FFB300", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Bright and cheerful yellow for a radiant day.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#FFB300", decoration = "SUNNY"
                ),
                Theme(
                    id = "6", name = "Sky", collection = "Up in the clouds", price = 1000,
                    thumbnailUrl = "#29B6F6", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Vibrant sky blue for a fresh perspective.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#29B6F6", decoration = "SKY"
                ),
                Theme(
                    id = "7", name = "Forest", collection = "Nature's embrace", price = 1000,
                    thumbnailUrl = "#26A69A", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Deep teal inspired by the tranquility of nature.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#26A69A", decoration = "FOREST"
                ),
                Theme(
                    id = "8", name = "Coffee", collection = "Morning brew", price = 1200,
                    thumbnailUrl = "#8D6E63", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Warm coffee-toned theme for cozy moments.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#8D6E63", decoration = "COFFEE"
                ),
                Theme(
                    id = "10", name = "Lemon", collection = "Zesty life", price = 1000,
                    thumbnailUrl = "#CDDC39", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Fresh and zesty citrus for a bright day.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#CDDC39", decoration = "LEMON"
                ),
                Theme(
                    id = "11", name = "Cherry", collection = "Sweet & Red", price = 1000,
                    thumbnailUrl = "#EF5350", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Sweet cherry red to express your passion.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#EF5350", decoration = "CHERRY"
                ),
                Theme(
                    id = "12", name = "Lavender", collection = "Calm Purple", price = 1000,
                    thumbnailUrl = "#B570EB", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Soothing lavender for peaceful reflection.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#B570EB", decoration = "LAVENDER"
                ),
                Theme(
                    id = "13", name = "Ocean", collection = "Deep Blue Sea", price = 1000,
                    thumbnailUrl = "#66AAEB", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Deep ocean blue for your deepest thoughts.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#66AAEB", decoration = "OCEAN"
                ),
                Theme(
                    id = "14", name = "Nebula", collection = "Cosmic dream", price = 2000,
                    thumbnailUrl = "#BA68C8", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Deep purple and pink celestial cosmic theme.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#BA68C8", decoration = "NEBULA"
                ),
                Theme(
                    id = "15", name = "Matcha", collection = "Zen Garden", price = 1500,
                    thumbnailUrl = "#A5D6A7", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Soft green tones for a peaceful mind.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#A5D6A7", decoration = "MATCHA"
                ),
                Theme(
                    id = "16", name = "Sunset", collection = "Golden Sky", price = 1800,
                    thumbnailUrl = "#FFB74D", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Warm orange and red like the setting sun.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#FFB74D", decoration = "SUNSET"
                ),
                Theme(
                    id = "17", name = "Galaxy", collection = "Stardust Journey", price = 2500,
                    thumbnailUrl = "#9FA8DA", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Indigo and blue mystery of the far galaxy.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#9FA8DA", decoration = "GALAXY"
                ),
                Theme(
                    id = "18", name = "Autumn", collection = "Fall Season", price = 1200,
                    thumbnailUrl = "#FFAB91", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Earthy brown and orange of falling leaves.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#FFAB91", decoration = "AUTUMN"
                ),
                Theme(
                    id = "19", name = "Gray Brown", collection = "Minimalist Earth", price = 900,
                    thumbnailUrl = "#BCAAA4", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Sophisticated neutral tones for minimalism.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#BCAAA4", decoration = "GRAY_BROWN"
                ),
                Theme(
                    id = "20", name = "Cookie Batch", collection = "Sweet Treats", price = 1300,
                    thumbnailUrl = "#FFFFD54F", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Warm cookie colors for a sweet session.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#FFFFD54F", decoration = "COOKIE_BATCH"
                ),
                Theme(
                    id = "21", name = "Heart Felt", collection = "Romantic Softness", price = 1400,
                    thumbnailUrl = "#F06292", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Express love with sweet soft pinks.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#F06292", decoration = "HEART_FELT"
                ),
                Theme(
                    id = "22", name = "Weather Cycle", collection = "Dynamic Nature", price = 1100,
                    thumbnailUrl = "#90A4AE", backgroundUrl = "#F4F6F1",
                    type = ThemeType.THEME, description = "Clean gray-blue tones of the weather.",
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#90A4AE", decoration = "WEATHER_CYCLE"
                )
            )

            val mockOwnedThemes = listOf(
                Theme(
                    id = "9",
                    name = "Classic Yellow",
                    collection = "Original style",
                    price = 0,
                    thumbnailUrl = "#FFFFC547",
                    backgroundUrl = "#F4F6F1",
                    isActive = true,
                    isOwned = true,
                    type = ThemeType.THEME,
                    icons = listOf("VERY_HAPPY", "HAPPY", "NEUTRAL", "SAD", "ANGRY"),
                    primaryColor = "#FFFFC547"
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
            val themeType = when (themeId) {
                "1" -> MoonThemeType.BLUSHING
                "2" -> MoonThemeType.KITTY
                "3" -> MoonThemeType.SPROUT
                "4" -> MoonThemeType.MIDNIGHT
                "12" -> MoonThemeType.SUNNY
                "13" -> MoonThemeType.SKY
                "14" -> MoonThemeType.FOREST
                "15" -> MoonThemeType.COFFEE
                "16" -> MoonThemeType.LEMON
                "17" -> MoonThemeType.CHERRY
                "18" -> MoonThemeType.LAVENDER
                "19" -> MoonThemeType.OCEAN
                "20" -> MoonThemeType.NEBULA
                "21" -> MoonThemeType.MATCHA
                "22" -> MoonThemeType.SUNSET
                "23" -> MoonThemeType.GALAXY
                "24" -> MoonThemeType.AUTUMN
                "6" -> MoonThemeType.GRAY_BROWN
                "7" -> MoonThemeType.COOKIE_BATCH
                "8" -> MoonThemeType.HEART_FELT
                "11" -> MoonThemeType.WEATHER_CYCLE
                else -> MoonThemeType.DEFAULT
            }
            themePreferencesManager.setThemeType(themeType)
            
            _uiEffect.send(StoreUiEffect.ThemeActivated)
            _uiEffect.send(StoreUiEffect.ShowSnackBar("Theme activated!"))
        }
    }
}
