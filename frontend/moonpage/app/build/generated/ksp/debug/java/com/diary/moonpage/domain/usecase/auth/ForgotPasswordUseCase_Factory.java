package com.diary.moonpage.domain.usecase.auth;

import com.diary.moonpage.domain.repository.AuthRepository;
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
public final class ForgotPasswordUseCase_Factory implements Factory<ForgotPasswordUseCase> {
  private final Provider<AuthRepository> repositoryProvider;

  public ForgotPasswordUseCase_Factory(Provider<AuthRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ForgotPasswordUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ForgotPasswordUseCase_Factory create(Provider<AuthRepository> repositoryProvider) {
    return new ForgotPasswordUseCase_Factory(repositoryProvider);
  }

  public static ForgotPasswordUseCase newInstance(AuthRepository repository) {
    return new ForgotPasswordUseCase(repository);
  }
}
