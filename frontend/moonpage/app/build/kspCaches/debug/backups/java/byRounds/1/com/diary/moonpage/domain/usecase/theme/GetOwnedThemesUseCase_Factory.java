package com.diary.moonpage.domain.usecase.theme;

import com.diary.moonpage.domain.repository.ThemeRepository;
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
public final class GetOwnedThemesUseCase_Factory implements Factory<GetOwnedThemesUseCase> {
  private final Provider<ThemeRepository> repositoryProvider;

  public GetOwnedThemesUseCase_Factory(Provider<ThemeRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetOwnedThemesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetOwnedThemesUseCase_Factory create(Provider<ThemeRepository> repositoryProvider) {
    return new GetOwnedThemesUseCase_Factory(repositoryProvider);
  }

  public static GetOwnedThemesUseCase newInstance(ThemeRepository repository) {
    return new GetOwnedThemesUseCase(repository);
  }
}
