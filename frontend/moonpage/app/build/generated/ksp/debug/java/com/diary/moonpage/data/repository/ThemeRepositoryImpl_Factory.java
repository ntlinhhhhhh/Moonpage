package com.diary.moonpage.data.repository;

import com.diary.moonpage.data.remote.api.ThemeApi;
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
public final class ThemeRepositoryImpl_Factory implements Factory<ThemeRepositoryImpl> {
  private final Provider<ThemeApi> apiProvider;

  public ThemeRepositoryImpl_Factory(Provider<ThemeApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public ThemeRepositoryImpl get() {
    return newInstance(apiProvider.get());
  }

  public static ThemeRepositoryImpl_Factory create(Provider<ThemeApi> apiProvider) {
    return new ThemeRepositoryImpl_Factory(apiProvider);
  }

  public static ThemeRepositoryImpl newInstance(ThemeApi api) {
    return new ThemeRepositoryImpl(api);
  }
}
