package com.diary.moonpage.presentation.screens.auth;

import com.diary.moonpage.core.util.TokenManager;
import com.diary.moonpage.domain.usecase.auth.ForgotPasswordUseCase;
import com.diary.moonpage.domain.usecase.auth.GoogleLoginUseCase;
import com.diary.moonpage.domain.usecase.auth.LoginUseCase;
import com.diary.moonpage.domain.usecase.auth.RegisterUserCase;
import com.diary.moonpage.domain.usecase.auth.ResetPasswordUseCase;
import com.diary.moonpage.domain.usecase.auth.VerifyOtpUseCase;
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<LoginUseCase> loginUseCaseProvider;

  private final Provider<RegisterUserCase> registerUseCaseProvider;

  private final Provider<GoogleLoginUseCase> googleLoginUseCaseProvider;

  private final Provider<ForgotPasswordUseCase> forgotPasswordUseCaseProvider;

  private final Provider<VerifyOtpUseCase> verifyOtpUseCaseProvider;

  private final Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  public AuthViewModel_Factory(Provider<LoginUseCase> loginUseCaseProvider,
      Provider<RegisterUserCase> registerUseCaseProvider,
      Provider<GoogleLoginUseCase> googleLoginUseCaseProvider,
      Provider<ForgotPasswordUseCase> forgotPasswordUseCaseProvider,
      Provider<VerifyOtpUseCase> verifyOtpUseCaseProvider,
      Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider,
      Provider<TokenManager> tokenManagerProvider) {
    this.loginUseCaseProvider = loginUseCaseProvider;
    this.registerUseCaseProvider = registerUseCaseProvider;
    this.googleLoginUseCaseProvider = googleLoginUseCaseProvider;
    this.forgotPasswordUseCaseProvider = forgotPasswordUseCaseProvider;
    this.verifyOtpUseCaseProvider = verifyOtpUseCaseProvider;
    this.resetPasswordUseCaseProvider = resetPasswordUseCaseProvider;
    this.tokenManagerProvider = tokenManagerProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(loginUseCaseProvider.get(), registerUseCaseProvider.get(), googleLoginUseCaseProvider.get(), forgotPasswordUseCaseProvider.get(), verifyOtpUseCaseProvider.get(), resetPasswordUseCaseProvider.get(), tokenManagerProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<LoginUseCase> loginUseCaseProvider,
      Provider<RegisterUserCase> registerUseCaseProvider,
      Provider<GoogleLoginUseCase> googleLoginUseCaseProvider,
      Provider<ForgotPasswordUseCase> forgotPasswordUseCaseProvider,
      Provider<VerifyOtpUseCase> verifyOtpUseCaseProvider,
      Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider,
      Provider<TokenManager> tokenManagerProvider) {
    return new AuthViewModel_Factory(loginUseCaseProvider, registerUseCaseProvider, googleLoginUseCaseProvider, forgotPasswordUseCaseProvider, verifyOtpUseCaseProvider, resetPasswordUseCaseProvider, tokenManagerProvider);
  }

  public static AuthViewModel newInstance(LoginUseCase loginUseCase,
      RegisterUserCase registerUseCase, GoogleLoginUseCase googleLoginUseCase,
      ForgotPasswordUseCase forgotPasswordUseCase, VerifyOtpUseCase verifyOtpUseCase,
      ResetPasswordUseCase resetPasswordUseCase, TokenManager tokenManager) {
    return new AuthViewModel(loginUseCase, registerUseCase, googleLoginUseCase, forgotPasswordUseCase, verifyOtpUseCase, resetPasswordUseCase, tokenManager);
  }
}
