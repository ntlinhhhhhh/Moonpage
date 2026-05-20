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
public final class RegisterUserCase_Factory implements Factory<RegisterUserCase> {
  private final Provider<AuthRepository> repositoryProvider;

  public RegisterUserCase_Factory(Provider<AuthRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public RegisterUserCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static RegisterUserCase_Factory create(Provider<AuthRepository> repositoryProvider) {
    return new RegisterUserCase_Factory(repositoryProvider);
  }

  public static RegisterUserCase newInstance(AuthRepository repository) {
    return new RegisterUserCase(repository);
  }
}
