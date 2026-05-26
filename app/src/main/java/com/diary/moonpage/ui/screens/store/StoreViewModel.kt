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
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.ui.components.feedback.SnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val getThemesUseCase: GetThemesUseCase,
    private val getOwnedThemesUseCase: GetOwnedThemesUseCase,
    private val buyThemeUseCase: BuyThemeUseCase,
    private val setActiveThemeUseCase: SetActiveThemeUseCase,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val themeRepository: com.diary.moonpage.domain.repository.ThemeRepository,
    private val statisticsRepository: com.diary.moonpage.domain.repository.StatisticsRepository,
    @ApplicationContext private val context: Context
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
                    streakFreezeCount = user?.streakFreezeCount ?: 0,
                    currentStreak = user?.currentStreak ?: 0
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

        viewModelScope.launch {
            themeRepository.myThemes.collect { customThemes ->
                _uiState.update { it.copy(customThemes = customThemes.newestCreatedFirst()) }
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

    fun showRenameCustomThemeDialog(theme: Theme) {
        _uiState.update { it.copy(themeToRename = theme) }
    }

    fun dismissRenameCustomThemeDialog() {
        _uiState.update { it.copy(themeToRename = null, isRenamingTheme = false) }
    }

    fun renameCustomTheme(name: String) {
        val theme = _uiState.value.themeToRename ?: return
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return
        performRenameCustomTheme(theme, trimmedName)
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
                    showRecoverySuccessDialog = false,
                    showConfirmCustomThemeUnlockDialog = false,
                    showInsufficientCoinsSheet = false,
                    themeToRename = null,
                    isRenamingTheme = false
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
            StoreUiEvent.InitiateCustomThemeUnlock -> initiateCustomThemeUnlock()
            StoreUiEvent.ConfirmCustomThemeUnlock -> unlockCustomThemeSlot()
            StoreUiEvent.CancelCustomThemeUnlock -> {
                _uiState.update { it.copy(showConfirmCustomThemeUnlockDialog = false) }
            }
            StoreUiEvent.DismissInsufficientCoins -> {
                _uiState.update { it.copy(showInsufficientCoinsSheet = false) }
            }
        }
    }

    private fun initiateCustomThemeUnlock() {
        if (_uiState.value.userCoins < CUSTOM_THEME_SLOT_PRICE) {
            _uiState.update { it.copy(showInsufficientCoinsSheet = true) }
        } else {
            _uiState.update { it.copy(showConfirmCustomThemeUnlockDialog = true) }
        }
    }

    fun unlockCustomThemeSlot() {
        viewModelScope.launch {
            _uiState.update { it.copy(showConfirmCustomThemeUnlockDialog = false) }
            _uiEffect.emit(StoreUiEffect.NavigateToCustomThemeEditor)
        }
    }

    private fun performRecoverStreak() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.recoverStreak().onSuccess {
                refreshStreakRelatedData()
                _uiState.update { it.copy(
                    isLoading = false, 
                    showRecoverySuccessDialog = true,
                    recoveryMessageRes = R.string.store_streak_recovered_success
                ) }
                _uiEffect.emit(StoreUiEffect.RecoverSuccess)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.emit(
                    StoreUiEffect.ShowSnackBar(
                        error.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.recovery_failed),
                        SnackbarType.ERROR
                    )
                )
            }
        }
    }

    private fun performBuyStreakFreeze() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmFreezePurchaseDialog = false) }
            userRepository.buyStreakFreeze().onSuccess {
                refreshStreakRelatedData()
                _uiState.update { it.copy(isLoading = false, freezePurchaseSuccess = true) }
                _uiEffect.emit(StoreUiEffect.PurchaseSuccess)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.emit(
                    StoreUiEffect.ShowSnackBar(
                        error.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.purchase_failed),
                        SnackbarType.ERROR
                    )
                )
            }
        }
    }

    fun dismissSuccessMessage() {
        _uiState.update { it.copy(activationSuccess = false) }
    }

    private suspend fun refreshStreakRelatedData() {
        userRepository.getCurrentUser()
        statisticsRepository.triggerRefresh()
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
                    _uiEffect.emit(
                        StoreUiEffect.ShowSnackBar(
                            error.message?.let(UiText::DynamicString)
                                ?: UiText.StringResource(R.string.failed_load_store_themes),
                            SnackbarType.ERROR
                        )
                    )
                }
            }

            // 3. Fetch Owned Themes (Background refresh)
            getOwnedThemesUseCase().onFailure { error ->
                if (_uiState.value.ownedThemes.isEmpty()) {
                    _uiEffect.emit(
                        StoreUiEffect.ShowSnackBar(
                            error.message?.let(UiText::DynamicString)
                                ?: UiText.StringResource(R.string.failed_load_owned_themes),
                            SnackbarType.ERROR
                        )
                    )
                }
            }

            // 4. Fetch custom themes created by the current user
            themeRepository.getMyThemes().onFailure { error ->
                _uiEffect.emit(
                    StoreUiEffect.ShowSnackBar(
                        error.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.failed_load_custom_themes),
                        SnackbarType.ERROR
                    )
                )
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
                _uiEffect.emit(
                    StoreUiEffect.ShowSnackBar(
                        error.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.purchase_failed),
                        SnackbarType.ERROR
                    )
                )
            }
        }
    }

    private fun performActivateTheme(themeId: String) {
        viewModelScope.launch {
            // Optimistically update the UI state in the store screen
            val theme = (_uiState.value.ownedThemes + _uiState.value.customThemes)
                .distinctBy { it.id }
                .find { it.id == themeId }
            
            _uiState.update { state ->
                val updatedOwned = state.ownedThemes.map { t ->
                    t.copy(isActive = t.id == themeId)
                }
                val updatedCustomThemes = state.customThemes.map { t ->
                    t.copy(isActive = t.id == themeId)
                }
                val updatedDetail = state.selectedThemeDetail?.copy(
                    isActive = state.selectedThemeDetail.id == themeId
                )

                state.copy(
                    ownedThemes = updatedOwned,
                    customThemes = updatedCustomThemes,
                    selectedThemeDetail = updatedDetail,
                    activationSuccess = true
                )
            }

            // Emit effect immediately for instant feedback
            _uiEffect.emit(
                StoreUiEffect.ThemeActivated(
                    message = theme?.name?.let {
                        UiText.StringResource(R.string.theme_activated_success, it)
                    } ?: UiText.StringResource(R.string.theme_updated_success)
                )
            )

            // Call the UseCase (which now handles optimistic DB/DataStore updates)
            setActiveThemeUseCase(themeId).onFailure { error ->
                _uiEffect.emit(
                    StoreUiEffect.ShowSnackBar(
                        error.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.failed_sync_theme_activation),
                        SnackbarType.ERROR
                    )
                )
            }
        }
    }

    private fun performRenameCustomTheme(theme: Theme, name: String) {
        viewModelScope.launch {
            val themeId = theme.id.trim()
            if (themeId.isBlank()) {
                _uiEffect.emit(
                    StoreUiEffect.ShowSnackBar(
                        UiText.StringResource(R.string.theme_id_missing),
                        SnackbarType.WARNING
                    )
                )
                return@launch
            }
            _uiState.update { it.copy(isRenamingTheme = true) }
            themeRepository.renameTheme(themeId, name).onSuccess {
                _uiState.update { state ->
                    val rename: (Theme) -> Theme = { current ->
                        if (current.id == themeId) current.copy(name = name) else current
                    }
                    state.copy(
                        customThemes = state.customThemes.map(rename),
                        ownedThemes = state.ownedThemes.map(rename),
                        themes = state.themes.map(rename),
                        selectedThemeDetail = state.selectedThemeDetail?.let(rename),
                        purchasedTheme = state.purchasedTheme?.let(rename),
                        themeToPurchase = state.themeToPurchase?.let(rename),
                        themeToRename = null,
                        isRenamingTheme = false
                    )
                }
                _uiEffect.emit(
                    StoreUiEffect.ShowSnackBar(
                        UiText.StringResource(R.string.theme_renamed),
                        SnackbarType.SUCCESS
                    )
                )
            }.onFailure { error ->
                _uiState.update { it.copy(isRenamingTheme = false) }
                _uiEffect.emit(
                    StoreUiEffect.ShowSnackBar(
                        error.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.failed_rename_theme),
                        SnackbarType.ERROR
                    )
                )
            }
        }
    }
}

const val CUSTOM_THEME_SLOT_PRICE = 250

private fun List<Theme>.newestCreatedFirst(): List<Theme> {
    return mapIndexed { index, theme -> index to theme }
        .sortedWith(
            compareByDescending<Pair<Int, Theme>> { (_, theme) ->
                theme.customThemeCreatedAtMillis() ?: Long.MIN_VALUE
            }.thenBy { (index, _) -> index }
        )
        .map { (_, theme) -> theme }
}

private fun Theme.customThemeCreatedAtMillis(): Long? {
    id.substringAfterLast('_')
        .toLongOrNull()
        ?.takeIf { it.isLikelyCustomThemeTimestamp() }
        ?.let { return it }

    return listOfNotNull(thumbnailUrl, backgroundUrl, description)
        .asSequence()
        .flatMap { value ->
            CUSTOM_THEME_FILE_TIMESTAMP_REGEX.findAll(value)
                .mapNotNull { match -> match.groupValues.getOrNull(1)?.toLongOrNull() }
        }
        .filter { it.isLikelyCustomThemeTimestamp() }
        .maxOrNull()
}

private fun Long.isLikelyCustomThemeTimestamp(): Boolean = this >= 1_000_000_000_000L

private val CUSTOM_THEME_FILE_TIMESTAMP_REGEX = Regex("""custom_theme_(?:thumb|bg)_(\d{10,})""")

// Extension to map Theme to MoonThemeType
fun Theme.toMoonThemeType(): MoonThemeType {
    if (this.id == ThemeConstants.DEFAULT_THEME_ID) return MoonThemeType.DEFAULT
    return id.toMoonThemeTypeOrNull()
        ?: decoration.toMoonThemeTypeOrNull()
        ?: MoonThemeType.DEFAULT
}

private fun String.toMoonThemeTypeOrNull(): MoonThemeType? {
    if (this == ThemeConstants.DEFAULT_THEME_ID) return MoonThemeType.DEFAULT
    val normalized = if (startsWith("theme_")) substringAfter("theme_") else this
    val enumName = when (normalized.uppercase()) {
        "MOON" -> "DEFAULT"
        "BROWN" -> "GRAY_BROWN"
        "COOKIE" -> "COOKIE_BATCH"
        "HEART" -> "HEART_FELT"
        "WEATHER" -> "WEATHER_CYCLE"
        else -> normalized.uppercase()
    }
    return runCatching { MoonThemeType.valueOf(enumName) }.getOrNull()
}
