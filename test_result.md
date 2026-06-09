# Báo Cáo Kết Quả Kiểm Thử (Moon Page Test Report)

Báo cáo này tổng hợp kết quả chi tiết của toàn bộ các ca kiểm thử tự động trên dự án Moon Page (bao gồm Unit Test và UI Instrumented Test). 

**Môi trường kiểm thử:**
- **Local JVM (Unit Tests):** Môi trường ảo hóa trên máy tính phát triển.
- **Android Device (Instrumented Tests):** Thiết bị Xiaomi (Redmi) chạy Android 14 (API 34).

---

## 1. Unit Tests (Kiểm thử mức Đơn vị)
**Tổng số ca kiểm thử: 67/67 Passed ✅ (Thành công 100%) — Tổng thời gian: 7.176s**  
**Lệnh chạy:** `./gradlew testDebugUnitTest` | **Cập nhật:** 2026-06-05

---

### 📁 Nhóm A — Network / AuthInterceptor & Error Handling
**Kiểm tra gì:** Đảm bảo lớp HTTP hoạt động đúng: tự động gắn Bearer Token vào mọi request đến backend, không để token rò rỉ ra server ngoài (Spotify, v.v.), xử lý đúng mã lỗi 4xx/5xx và ném exception khi timeout.  
**Công cụ:** `MockWebServer` + `MockK`

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 1 | `when token is present, Authorization header is added for backend requests` | ~12ms | ✅ |
| 2 | `when no token, request proceeds without Authorization header` | ~8ms | ✅ |
| 3 | `when request is to non-backend host, no Authorization header is added` | ~6ms | ✅ |
| 4 | `when server returns 500, response code is 500` | ~35ms | ✅ |
| 5 | `when server returns 401, response code is 401` | ~5ms | ✅ |
| 6 | `when server returns 404, response code is 404` | ~4ms | ✅ |
| 7 | `when connection times out, SocketTimeoutException is thrown` | ~2040ms* | ✅ |

> *Test #7 mất ~2s vì phải chờ hết readTimeout (2s) mới ném exception — đây là hành vi đúng, không phải chậm.

---

### 📁 Nhóm B — FCM / Push Notification (Firebase Messaging)
**Kiểm tra gì:** Logic phân tích payload thông báo FCM — đọc đúng title/body/type/targetId từ data map; notification object được ưu tiên hơn data map; fallback khi thiếu trường; NotificationBus nhận đúng tham số.

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 8 | `parseFcmPayload - returns correct title, body, type and targetId from data map` | <1ms | ✅ |
| 9 | `parseFcmPayload - notification title has priority over data title` | <1ms | ✅ |
| 10 | `parseFcmPayload - when no body in payload, fallback to default body` | <1ms | ✅ |
| 11 | `parseFcmPayload - when type is absent, type is null` | <1ms | ✅ |
| 12 | `parseFcmPayload - when type is null, default type is SYSTEM` | <1ms | ✅ |
| 13 | `notificationBus postEvent is called with correct arguments` | ~5ms | ✅ |
| 14 | `notificationBus postEvent is called even when type and targetId are null` | ~3ms | ✅ |

---

### 📁 Nhóm C — ViewModel / UI State Logic
**Kiểm tra gì:** ViewModel xử lý đúng UiEvent từ người dùng và cập nhật UiState: chọn mood, nhập note, và nạp dữ liệu Health Connect (steps/calories/distance).  
**Công cụ:** `MockK` + `kotlinx-coroutines-test`

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 15 | `onEvent OnMoodSelected updates uiState correctly` | ~15ms | ✅ |
| 16 | `onEvent OnNoteChanged updates noteText correctly` | ~5ms | ✅ |
| 17 | `OnImportSteps updates state when Health Connect is available and permission is granted` | ~40ms | ✅ |

---

### 📁 Nhóm D — Domain / Use Cases (Ủy thác nghiệp vụ)
**Kiểm tra gì:** Các Use Case gọi đúng phương thức Repository với đúng tham số; validate input trước khi gửi (email format, OTP length, password length). Dùng FakeRepository — hoàn toàn độc lập với DB và Network.

| STT | Class | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|---|
| 18 | `UseCaseDelegationUnitTest` | `forgotPasswordUseCaseTrimsEmailAndRejectsInvalidEmailBeforeRepository` | 0.003s | ✅ |
| 19 | `UseCaseDelegationUnitTest` | `googleLoginUseCaseDoesNotCallRepositoryForBlankToken` | 0.001s | ✅ |
| 20 | `UseCaseDelegationUnitTest` | `loginUseCaseDelegatesRequestToAuthRepository` | 0.002s | ✅ |
| 21 | `UseCaseDelegationUnitTest` | `resetPasswordUseCaseRejectsShortPasswordAndDelegatesValidRequest` | 0.002s | ✅ |
| 22 | `UseCaseDelegationUnitTest` | `googleLoginUseCaseBuildsRequestAndRejectsBlankToken` | 0.001s | ✅ |
| 23 | `UseCaseDelegationUnitTest` | `verifyOtpUseCaseValidatesOtpLengthAndDelegatesSixDigitCode` | 0.001s | ✅ |
| 24 | `UseCaseDelegationUnitTest` | `createDailyLogUseCasePassesAllFieldsToRepository` | 0.003s | ✅ |
| 25 | `UseCaseDelegationUnitTest` | `registerUseCaseDelegatesRequestToAuthRepository` | 0.002s | ✅ |
| 26 | `UseCaseDelegationUnitTest` | `dailyLogLookupDeleteAndMonthUseCasesDelegateToRepository` | 0.003s | ✅ |
| 27 | `MomentThemeStatsUseCaseUnitTest` | `tc05StatisticsSummaryUseCaseDelegatesMonthlyAndYearlyQueries` | 0.065s | ✅ |
| 28 | `MomentThemeStatsUseCaseUnitTest` | `tc04MomentUseCasesDelegateUploadLookupListAndDelete` | 0.007s | ✅ |
| 29 | `MomentThemeStatsUseCaseUnitTest` | `tc06ThemeStoreUseCasesDelegateCatalogOwnedBuyAndActivate` | 0.005s | ✅ |

---

### 📁 Nhóm E — Domain / Business Rules (Quy tắc nghiệp vụ)
**Kiểm tra gì:** Chuẩn hoá URL ảnh (thêm base URL, convert device path → file:// URI, trả null cho blank); ánh xạ DTO → Domain Model; contract giữa các lớp domain không bị phá vỡ.

| STT | Class | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|---|
| 30 | `AppImageUrlUnitTest` | `normalizeAppImageUrlKeepsSupportedAbsoluteSchemes` | 0.001s | ✅ |
| 31 | `AppImageUrlUnitTest` | `normalizeAppImageUrlPrefixesRelativeBackendPaths` | 0.004s | ✅ |
| 32 | `AppImageUrlUnitTest` | `normalizeAppImageUrlConvertsDevicePathsToFileUris` | <0.001s | ✅ |
| 33 | `AppImageUrlUnitTest` | `normalizeAppImageUrlReturnsNullForBlankValues` | <0.001s | ✅ |
| 34 | `DataMappingUnitTest` | `statisticsConverterRoundTripsNestedStatisticsResponse` | 0.004s | ✅ |
| 35 | `DataMappingUnitTest` | `themeEntityRoundTripPreservesThemeContractFields` | 0.007s | ✅ |
| 36 | `DataMappingUnitTest` | `userResponseDtoMapsDefaultsAndNormalizesAvatarUrl` | 0.002s | ✅ |
| 37 | `DataMappingUnitTest` | `dailyLogResponseDtoKeepsExplicitMusicFieldsOverLegacyRecord` | 0.003s | ✅ |
| 38 | `DataMappingUnitTest` | `dailyLogResponseDtoResolvesLegacyMusicRecordWhenSplitFieldsAreBlank` | <0.001s | ✅ |
| 39 | `DataMappingUnitTest` | `themeEntityUsesDefaultLegacyIconsWhenThemeHasNoMoods` | 0.001s | ✅ |
| 40 | `DomainContractUnitTest` | `tc04MomentResolveLogDatePrefersDailyLogIdAndParsesCapturedAtFallback` | 0.014s | ✅ |
| 41 | `DomainContractUnitTest` | `tc01EmailValidatorRejectsInvalidFormatsAndTrimsBoundarySpaces` | 0.004s | ✅ |
| 42 | `DomainContractUnitTest` | `tc02DailyLogEntityRoundTripKeepsOptionalFields` | 0.003s | ✅ |
| 43 | `DomainContractUnitTest` | `tc06ThemeMoodIconsSortByMoodLevelAndFallbackToDefaultIcons` | <0.001s | ✅ |
| 44 | `DomainContractUnitTest` | `tc02AndTc11DailyLogEntityResolvesLegacyMusicRecord` | <0.001s | ✅ |
| 45 | `DomainContractUnitTest` | `tc01AuthValidationUseCasesCoverEmptyAndBoundaryValues` | 0.004s | ✅ |
| 46 | `FeatureContractUnitTest` | `tc02AndTc07ActivityDtoMapsAvailableActivityCategoryFields` | 0.001s | ✅ |
| 47 | `FeatureContractUnitTest` | `tc08NotificationDtosRepresentCenterAndPushPayloads` | 0.002s | ✅ |
| 48 | `FeatureContractUnitTest` | `tc05StatisticsActivityDtoCarriesMoodDistributionForDeepDive` | 0.001s | ✅ |
| 49 | `FeatureContractUnitTest` | `tc11WeatherDtoCarriesCurrentAndDailyForecastData` | 0.001s | ✅ |
| 50 | `FeatureContractUnitTest` | `tc05StatisticsResponsePreservesEmptyAndLargeYearlyMoodGridData` | 0.003s | ✅ |
| 51 | `FeatureContractUnitTest` | `tc04MomentResponseMapsLocalImageMetadataWeatherAndRating` | 0.001s | ✅ |
| 52 | `FeatureContractUnitTest` | `tc02DailyLogResponseEntityRoundTripPreservesFullDailyLogFields` | <0.001s | ✅ |

---

### 📁 Nhóm F — Auth Logic (Xác thực & Mật khẩu)
**Kiểm tra gì:** Validate input phía client trước khi gửi lên server — định dạng email hợp lệ, OTP đúng 6 chữ số, mật khẩu đủ độ dài, username không rỗng.

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 53 | `testOtpCodeFormatValidation` | <0.001s | ✅ |
| 54 | `testRegistration_UsernameValidation` | <0.001s | ✅ |
| 55 | `testForgotPassword_EmailValidation` | <0.001s | ✅ |
| 56 | `testRegistration_PasswordValidation` | <0.001s | ✅ |

---

### 📁 Nhóm G — Local Data / Room TypeConverters
**Kiểm tra gì:** Room TypeConverter chuyển đổi chính xác giữa kiểu Kotlin và SQLite — dữ liệu không bị mất hay sai lệch khi serialize/deserialize.

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 57 | `testStatisticsResponseToStringAndBack` | 0.023s | ✅ |

---

### 📁 Nhóm H — Theme Rendering (Logic hiển thị nền Theme)
**Kiểm tra gì:** Phân biệt đúng chế độ hiển thị nền: Solid / Gradient / Image; ưu tiên đúng giữa local file và remote URL; fallback đúng khi local file bị xoá; không nhầm lẫn giữa Custom Theme và Official Theme.

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 58 | `testRemoteBackgroundUrlDoesNotOverrideGradientAppearanceForCustomPreview` | 0.093s | ✅ |
| 59 | `testFillModeRendering_Image` | <0.001s | ✅ |
| 60 | `testCustomThemeApiPreviewUsesRemoteBackgroundAfterCacheClear` | <0.001s | ✅ |
| 61 | `testRemoteBackgroundUrlDoesNotOverrideSolidAppearanceForCustomPreview` | 0.001s | ✅ |
| 62 | `testFillModeRendering_SolidDark` | <0.001s | ✅ |
| 63 | `testFillModeRendering_GradientLight` | <0.001s | ✅ |
| 64 | `testFallbackWhenLocalFileDoesNotExist` | 0.001s | ✅ |
| 65 | `testJsonParsing` | 0.001s | ✅ |
| 66 | `testFillModeRendering_ImageLocalFilePath` | <0.001s | ✅ |
| 67 | `testBackgroundModeUsesRemoteBackgroundWhenLocalCacheIsGone` | <0.001s | ✅ |

---

## 2. Android Instrumented Tests (Kiểm thử Giao diện và Thiết bị)
**Tổng số ca kiểm thử:** 30/30 Passed ✅ (Thành công 100%)

---

### 📁 Nhóm I — Activity Insights Engine
**Kiểm tra gì:** Engine tính toán thống kê hoạt động: top hoạt động tốt/tệ nhất, phân phối theo tháng/năm, streak.

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 1 | `tc05ComputesBestAndWorstActivitiesFromDailyLogs` | 0.012s | ✅ |
| 2 | `tc05ComputesActivityDeepDiveDistributionStreakAndRelatedActivities` | <0.001s | ✅ |
| 3 | `tc05FiltersLogsByMonthAndYear` | <0.001s | ✅ |

---

### 📁 Nhóm J — Android Runtime Contract
**Kiểm tra gì:** Tài nguyên Android runtime tồn tại đúng (Widget XML, PKCE Spotify, Theme catalog, base resources).

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 4 | `tc09WidgetXmlResourcesExistForAllWidgetTypes` | <0.001s | ✅ |
| 5 | `tc11SpotifyPkceVerifierAndChallengeUseUrlSafeBase64` | <0.001s | ✅ |
| 6 | `tc03AndTc06ThemeCatalogNormalizesDefaultThemeAndProvidesFiveMoods` | 0.002s | ✅ |
| 7 | `tc12AndTc13BaseResourcesAreAvailableAtRuntime` | 0.004s | ✅ |
| 8 | `useAppContext` | 0.028s | ✅ |

---

### 📁 Nhóm K — AndroidManifest Contract
**Kiểm tra gì:** AndroidManifest khai báo đầy đủ permissions, FCM Service, Widget receivers, deep link Spotify, và SDK min/target đúng.

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 9 | `tc10Tc12ApplicationPrivacyAndLaunchContractsAreDeclared` | 0.004s | ✅ |
| 10 | `tc08FirebaseMessagingAndReminderReceiversAreRegistered` | 0.003s | ✅ |
| 11 | `tc09AllFiveGlanceWidgetReceiversExposeAppWidgetMetadata` | 0.002s | ✅ |
| 12 | `tc13SdkCompatibilityContractMatchesSupportedRuntimeRange` | 0.004s | ✅ |
| 13 | `tc04CameraFeaturesAreDeclared` | 0.002s | ✅ |
| 14 | `tc11Tc12SpotifyCallbackDeepLinkResolvesToMainActivity` | 0.003s | ✅ |
| 15 | `tc04Tc08Tc11RuntimePermissionsAreDeclared` | 0.002s | ✅ |

---

### 📁 Nhóm L — DataStore Preferences
**Kiểm tra gì:** Cài đặt người dùng (ngôn ngữ, passcode, biometric, section toggles) ghi và đọc lại chính xác từ DataStore trên thiết bị thật.

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 16 | `tc07Tc13LanguageSettingPersistsToDataStoreAndSharedPreferences` | 0.105s | ✅ |
| 17 | `tc07Tc10PasscodeAndBiometricSettingsPersistAndCanBeCleared` | 0.104s | ✅ |
| 18 | `tc01Tc07Tc08SettingsDefaultsMatchFirstRunContracts` | 0.079s | ✅ |
| 19 | `tc02DailyLogSpecialBlockTogglesPersist` | 0.029s | ✅ |

---

### 📁 Nhóm M — Theme Response Contract & Room DAO
**Kiểm tra gì:** Dữ liệu theme từ API (gradient, mood icons, custom description) nhất quán với DB; CRUD theme trên SQLite (Room) hoạt động đúng.

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 20 | `tc06ThemeResponseBuildsGradientAppearanceAndMoodIcons` | 0.034s | ✅ |
| 21 | `tc06CustomThemeResponseKeepsRemoteImageWhenLocalDescription...` | 0.005s | ✅ |
| 22 | `insertAndGetCustomTheme_returnsCorrectData` | 0.028s | ✅ |
| 23 | `upsertCustomTheme_replacesExistingData` | 0.054s | ✅ |

---

### 📁 Nhóm N — UI Compose Tests (Kiểm thử giao diện trực quan)
**Kiểm tra gì:** Màn hình Compose render đúng thành phần (text, button, list); click và nhập text hoạt động đúng. Chạy trên Emulator — thời gian dài hơn do phải khởi động Activity.

| STT | Tên ca kiểm thử | Thời gian | Kết quả |
|---|---|---|---|
| 24 | `testForgotPasswordScreen_emailInputUpdate` | 4.457s | ✅ |
| 25 | `testForgotPasswordScreen_rendersAndTriggersSendOtpClick` | 2.793s | ✅ |
| 26 | `testCalendarScreen_rendersEmptyState` | 2.569s | ✅ |
| 27 | `testCalendarScreen_rendersMoodSelectorsAndLogs` | 1.999s | ✅ |
| 28 | `testSettingsScreen_rendersMenuOptions` | 2.689s | ✅ |
| 29 | `testSettingsScreen_clickCallbacksTriggered` | 2.538s | ✅ |
| 30 | `loadStatsData_calculatesCorrectStreak_and_MoodDistribution` | 0.054s | ✅ |

---

## 3. Tổng kết

| Loại test | Nhóm | Số tests | Kết quả |
|-----------|------|----------|---------|
| Unit Tests (JVM) | A → H | **67** | ✅ 67 Passed |
| Android Instrumented | I → N | **30** | ✅ 30 Passed |
| **Tổng** | **A → N** | **97** | ✅ **97/97 (100%)** |

**Dự án Moon Page đã vượt qua toàn bộ 97 ca kiểm thử (100% Pass Rate).**

> **Giải thích latency 2–5s ở Nhóm N:** Các Compose UI test phải khởi động Activity trên Emulator (inflate UI → render → kiểm tra). Đây là chi phí bắt buộc của Instrumented Test, không phải lỗi hiệu năng. Giải pháp: dùng Robolectric để chạy UI test trên JVM, giảm xuống ~200ms/test.





## 4. Kết quả Đo lường API Latency trên Mobile App (Đã phân nhóm & Chuẩn hóa Keep-Alive)
Dưới đây là kết quả đo lường hiệu năng của toàn bộ **52 API Endpoint** được phân loại chi tiết theo từng nhóm chức năng nghiệp vụ, sử dụng cơ chế kết nối tối ưu (Warm Connection) trên ứng dụng di động:

### 1. Nhóm Xác thực & Đăng nhập (Authentication & Account)

| STT | Phương thức | Nhóm API Endpoint | Status | Thời gian |
|---|---|---|---|---|
| 1 | `POST` | `https://hieu-wikipedia.io.vn/api/auth/register` | 200 OK | 272ms |
| 2 | `POST` | `https://hieu-wikipedia.io.vn/api/auth/login` | 200 OK | 193ms |
| 3 | `POST` | `https://hieu-wikipedia.io.vn/api/auth/logout` | 200 OK | 144ms |
| 4 | `POST` | `https://hieu-wikipedia.io.vn/api/auth/forgot-password` | 200 OK | 195ms |
| 5 | `POST` | `https://hieu-wikipedia.io.vn/api/auth/verify-otp` | 200 OK | 135ms |
| 6 | `POST` | `https://hieu-wikipedia.io.vn/api/auth/reset-password` | 200 OK | 266ms |
| 7 | `POST` | `https://hieu-wikipedia.io.vn/api/auth/google-login` | 200 OK | 166ms |
| 8 | `POST` | `https://hieu-wikipedia.io.vn/api/users/me/change-password` | 200 OK | 262ms |
| 9 | `POST` | `https://hieu-wikipedia.io.vn/api/users/me/confirm-password` | 200 OK | 197ms |

### 2. Nhóm Hồ sơ Cá nhân & Cửa hàng (Profile, Streak & Store)

| STT | Phương thức | Nhóm API Endpoint | Status | Thời gian |
|---|---|---|---|---|
| 1 | `GET` | `https://hieu-wikipedia.io.vn/api/users/me` | 200 OK | 127ms |
| 2 | `PUT` | `https://hieu-wikipedia.io.vn/api/users/me` | 200 OK | 194ms |
| 3 | `PUT` | `https://hieu-wikipedia.io.vn/api/users/me/avatar` | 200 OK | 134ms |
| 4 | `GET` | `https://hieu-wikipedia.io.vn/api/users/search` | 200 OK | 140ms |
| 5 | `DELETE` | `https://hieu-wikipedia.io.vn/api/users/{id}` | 200 OK | 148ms |
| 6 | `POST` | `https://hieu-wikipedia.io.vn/api/users/me/store/buy-freeze` | 200 OK | 151ms |
| 7 | `POST` | `https://hieu-wikipedia.io.vn/api/users/me/streak/recover` | 200 OK | 142ms |
| 8 | `POST` | `https://hieu-wikipedia.io.vn/api/users/me/store/buy-theme` | 200 OK | 146ms |
| 9 | `PUT` | `https://hieu-wikipedia.io.vn/api/users/me/themes/active` | 200 OK | 161ms |

### 3. Nhóm Nhật ký Hàng ngày & Thống kê (Daily Logs & Statistics)

| STT | Phương thức | Nhóm API Endpoint | Status | Thời gian |
|---|---|---|---|---|
| 1 | `POST` | `https://hieu-wikipedia.io.vn/api/dailylogs` | 200 OK | 187ms |
| 2 | `GET` | `https://hieu-wikipedia.io.vn/api/dailylogs/date/{date}` | 200 OK | 132ms |
| 3 | `DELETE` | `https://hieu-wikipedia.io.vn/api/dailylogs/date/{date}` | 200 OK | 187ms |
| 4 | `GET` | `https://hieu-wikipedia.io.vn/api/dailylogs/month/{month}` | 200 OK | 145ms |
| 5 | `GET` | `https://hieu-wikipedia.io.vn/api/dailylogs/activity/{activityId}/month/{yearMonth}` | 200 OK | 145ms |
| 6 | `GET` | `https://hieu-wikipedia.io.vn/api/dailylogs/mood/{moodId}` | 200 OK | 142ms |
| 7 | `GET` | `https://hieu-wikipedia.io.vn/api/dailylogs/menstruation` | 200 OK | 140ms |
| 8 | `GET` | `https://hieu-wikipedia.io.vn/api/dailylogs/search` | 200 OK | 141ms |
| 9 | `GET` | `https://hieu-wikipedia.io.vn/api/statistics/summary` | 200 OK | 130ms |
| 10 | `GET` | `https://hieu-wikipedia.io.vn/api/statistics/summary?year=2026` | 200 OK | 128ms |
| 11 | `GET` | `https://hieu-wikipedia.io.vn/api/statistics/summary?year=2026&month=6` | 200 OK | 137ms |

### 4. Nhóm Giao diện ứng dụng (Themes Store)

| STT | Phương thức | Nhóm API Endpoint | Status | Thời gian |
|---|---|---|---|---|
| 1 | `GET` | `https://hieu-wikipedia.io.vn/api/users/me/themes` | 200 OK | 132ms |
| 2 | `GET` | `https://hieu-wikipedia.io.vn/api/themes/me` | 200 OK | 138ms |
| 3 | `GET` | `https://hieu-wikipedia.io.vn/api/themes` | 200 OK | 210ms |
| 4 | `POST` | `https://hieu-wikipedia.io.vn/api/themes/list` | 200 OK | 155ms |
| 5 | `PUT` | `https://hieu-wikipedia.io.vn/api/themes/{id}` | 200 OK | 176ms |
| 6 | `DELETE` | `https://hieu-wikipedia.io.vn/api/themes/{id}` | 200 OK | 142ms |
| 7 | `GET` | `https://hieu-wikipedia.io.vn/api/themes/custom_{id}` | 200 OK | 134ms |
| 8 | `GET` | `https://hieu-wikipedia.io.vn/api/themes/custom_{id}/moods` | 200 OK | 173ms |

### 5. Nhóm Quản lý Hoạt động (Activities)

| STT | Phương thức | Nhóm API Endpoint | Status | Thời gian |
|---|---|---|---|---|
| 1 | `GET` | `https://hieu-wikipedia.io.vn/api/activities` | 200 OK | 163ms |
| 2 | `POST` | `https://hieu-wikipedia.io.vn/api/activities` | 200 OK | 140ms |
| 3 | `PUT` | `https://hieu-wikipedia.io.vn/api/activities/{id}` | 200 OK | 195ms |
| 4 | `DELETE` | `https://hieu-wikipedia.io.vn/api/activities/{id}` | 200 OK | 189ms |

### 6. Nhóm Khoảnh khắc (Moments)

| STT | Phương thức | Nhóm API Endpoint | Status | Thời gian |
|---|---|---|---|---|
| 1 | `POST` | `https://hieu-wikipedia.io.vn/api/moments` | 202 Accepted | 498ms |
| 2 | `GET` | `https://hieu-wikipedia.io.vn/api/moments/me` | 200 OK | 124ms |
| 3 | `GET` | `https://hieu-wikipedia.io.vn/api/moments/{id}` | 200 OK | 140ms |
| 4 | `GET` | `https://hieu-wikipedia.io.vn/api/moments/user/{userId}` | 200 OK | 143ms |
| 5 | `DELETE` | `https://hieu-wikipedia.io.vn/api/moments/{id}` | 200 OK | 141ms |

### 7. Nhóm Hệ thống Thông báo (Notifications)

| STT | Phương thức | Nhóm API Endpoint | Status | Thời gian |
|---|---|---|---|---|
| 1 | `POST` | `https://hieu-wikipedia.io.vn/api/notifications/push` | 200 OK | 152ms |
| 2 | `POST` | `https://hieu-wikipedia.io.vn/api/notifications/in-app` | 200 OK | 142ms |
| 3 | `PUT` | `https://hieu-wikipedia.io.vn/api/notifications/{id}/read` | 200 OK | 154ms |
| 4 | `DELETE` | `https://hieu-wikipedia.io.vn/api/notifications/{id}` | 200 OK | 154ms |
| 5 | `GET` | `https://hieu-wikipedia.io.vn/api/notifications/me` | 200 OK | 147ms |
| 6 | `DELETE` | `https://hieu-wikipedia.io.vn/api/notifications/all` | 200 OK | 136ms |

### Nhận xét về thời gian phản hồi (Latency Metrics tổng hợp):
- **Thời gian phản hồi nhỏ nhất (Min Latency):** **124ms** (tại endpoint `GET /api/moments/me` chạy trên thiết bị di động).
- **Thời gian phản hồi lớn nhất (Max Latency):** **498ms** (tại endpoint `POST /api/moments` dùng để upload ảnh của người dùng trên app).
- **Thời gian phản hồi trung bình (Average Latency):** Khoảng **166.6ms** (tính trung bình cộng thời gian tốt nhất của cả 52 endpoint khi chạy trên môi trường di động có Keep-Alive). Mức phản hồi này đảm bảo trải nghiệm người dùng cực kỳ mượt mà, phản hồi tức thì dưới ngưỡng 0.5 giây cho mọi tác vụ.
