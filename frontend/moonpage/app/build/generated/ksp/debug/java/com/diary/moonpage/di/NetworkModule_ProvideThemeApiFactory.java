package com.diary.moonpage.di;

import com.diary.moonpage.data.remote.api.ThemeApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NetworkModule_ProvideThemeApiFactory implements Factory<ThemeApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideThemeApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ThemeApi get() {
    return provideThemeApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideThemeApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideThemeApiFactory(retrofitProvider);
  }

  public static ThemeApi provideThemeApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideThemeApi(retrofit));
  }
}
