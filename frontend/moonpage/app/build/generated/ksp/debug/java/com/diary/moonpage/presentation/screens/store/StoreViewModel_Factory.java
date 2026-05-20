package com.diary.moonpage.presentation.screens.store;

import com.diary.moonpage.domain.usecase.theme.BuyThemeUseCase;
import com.diary.moonpage.domain.usecase.theme.GetOwnedThemesUseCase;
import com.diary.moonpage.domain.usecase.theme.GetThemesUseCase;
import com.diary.moonpage.domain.usecase.theme.SetActiveThemeUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class StoreViewModel_Factory implements Factory<StoreViewModel> {
  private final Provider<GetThemesUseCase> getThemesUseCaseProvider;

  private final Provider<GetOwnedThemesUseCase> getOwnedThemesUseCaseProvider;

  private final Provider<BuyThemeUseCase> buyThemeUseCaseProvider;

  private final Provider<SetActiveThemeUseCase> setActiveThemeUseCaseProvider;

  public StoreViewModel_Factory(Provider<GetThemesUseCase> getThemesUseCaseProvider,
      Provider<GetOwnedThemesUseCase> getOwnedThemesUseCaseProvider,
      Provider<BuyThemeUseCase> buyThemeUseCaseProvider,
      Provider<SetActiveThemeUseCase> setActiveThemeUseCaseProvider) {
    this.getThemesUseCaseProvider = getThemesUseCaseProvider;
    this.getOwnedThemesUseCaseProvider = getOwnedThemesUseCaseProvider;
    this.buyThemeUseCaseProvider = buyThemeUseCaseProvider;
    this.setActiveThemeUseCaseProvider = setActiveThemeUseCaseProvider;
  }

  @Override
  public StoreViewModel get() {
    return newInstance(getThemesUseCaseProvider.get(), getOwnedThemesUseCaseProvider.get(), buyThemeUseCaseProvider.get(), setActiveThemeUseCaseProvider.get());
  }

  public static StoreViewModel_Factory create(Provider<GetThemesUseCase> getThemesUseCaseProvider,
      Provider<GetOwnedThemesUseCase> getOwnedThemesUseCaseProvider,
      Provider<BuyThemeUseCase> buyThemeUseCaseProvider,
      Provider<SetActiveThemeUseCase> setActiveThemeUseCaseProvider) {
    return new StoreViewModel_Factory(getThemesUseCaseProvider, getOwnedThemesUseCaseProvider, buyThemeUseCaseProvider, setActiveThemeUseCaseProvider);
  }

  public static StoreViewModel newInstance(GetThemesUseCase getThemesUseCase,
      GetOwnedThemesUseCase getOwnedThemesUseCase, BuyThemeUseCase buyThemeUseCase,
      SetActiveThemeUseCase setActiveThemeUseCase) {
    return new StoreViewModel(getThemesUseCase, getOwnedThemesUseCase, buyThemeUseCase, setActiveThemeUseCase);
  }
}
