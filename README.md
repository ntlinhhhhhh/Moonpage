# Moon Page - Personal Mood Diary

[![Build Status](https://img.shields.io/badge/Build-Success-brightgreen.svg)]()
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-2024.10.00-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/compose)
[![Compile SDK](https://img.shields.io/badge/Compile_SDK-35-3DDC84.svg)]()

Moon Page is a modern Android diary app for recording daily mood, activities, photos, health signals, music, weather, and personal memories. The app combines mood tracking, rich calendar views, personalized themes, widgets, reminders, and privacy controls into one journaling workflow.

---

## Release APK

- [Download Moon Page 1.0 APK](app/release/moonpage_1.0.apk)

---

## Key Features

### Calendar & Daily Logging
- Daily journal entries with mood, note, activities, photos, music, weather, and health data.
- Five-level mood tracking using custom Cute Bean mood icons.
- Activity tagging with built-in categories such as emotions, meals, weather, health, hobbies, school, work, people, and self-care.
- Calendar and list views with filter support for moods, activities, music, weather, health, and photos.
- Menstrual cycle and sleep record support.
- Shareable daily log and monthly mood calendar images saved through native bitmap capture.

### Moments & Gallery
- CameraX-based in-app camera for capturing daily moments.
- Moment upload, delete, share, zoom preview, and public/private visibility controls.
- Photo gallery for browsing past memory photos.
- Photo Moment widget support for showing a selected daily image on the home screen.

### Statistics & Recaps
- Mood distribution, yearly mood detail, annual beans view, and year-in-pixels style summaries.
- Best/worst activity correlation, icon deep dive, and activity detail screens.
- Sleep and health statistics, including annual sleep/health summaries.
- Top music statistics and annual music detail.
- Streak tracking with current streak, longest streak, total days, and recovery support.

### Store & Personalization
- Theme Store with official themes, owned themes, collections, and custom themes.
- Moon Coins economy for buying themes and streak freeze items.
- Custom Theme Editor with:
  - Image, solid color, and gradient backgrounds.
  - Light/dark appearance configuration.
  - Drawing tools with brush styles, size control, color picker, undo, and clear.
  - Per-mood icon colors for all five mood icons.
  - Backend-synced custom theme moods so each icon can keep its own backend-defined color.
  - Custom themes sorted newest first in the store.
- Dynamic app theming for active themes, including theme-aware mood previews.
- Theme Calendar settings for choosing calendar display style.

### Widgets
- Android Glance widgets:
  - Quick Mood
  - Weekly Mood Calendar
  - Monthly Mood Calendar
  - Daily Summary
  - Photo Moment
- Widget customization screens with preview states and streak badge options.
- Widget refresh after daily log changes.

### Notifications & Reminders
- Firebase Cloud Messaging for push notifications.
- Notification Center for in-app notification history.
- Local daily reminders with exact alarms and boot-aware rescheduling.
- In-app notification/snackbar bus for real-time UI feedback.
- Streak, reminder, moment, and system notification flows.

### Security & Account
- Credential Manager and Google Identity support for Google sign-in.
- Passcode lock setup and verification.
- BiometricPrompt support for local fingerprint/face unlock.
- Profile, settings, account management, invite friend, language, and notification settings screens.

### Integrations
- Spotify integration for linking music to diary logs and viewing top/recent tracks.
- Health Connect import for steps, calories, distance, and sleep data.
- Location-based weather auto-fill with cached weather conditions.
- Google Places and Play Services Location support for location-aware flows.

---

## Architecture

Moon Page follows Clean Architecture with MVVM and state-hoisted Jetpack Compose screens.

### Layers

1. `core`: Dependency injection, theme system, utilities, network setup, preferences, notifications, and shared helpers.
2. `domain`: Business models, repository interfaces, and use cases.
3. `data`: Retrofit APIs, Room entities/DAOs, repository implementations, local cache, and remote DTO mapping.
4. `ui`: Compose screens, ViewModels, UI state/effects, navigation, components, and feature flows.
5. `widget`: Glance app widgets and widget data source.

---

## Tech Stack

- **Language:** Kotlin 2.1.0
- **Build:** Gradle, Android Gradle Plugin 8.13.2, KSP
- **Android:** minSdk 26, targetSdk 34, compileSdk 35, Java 17
- **UI:** Jetpack Compose, Compose BOM 2024.10.00, Material 3, Material Icons Extended
- **Navigation:** Jetpack Navigation Compose
- **State & Async:** Kotlin Coroutines, Flow, StateFlow, SharedFlow
- **Dependency Injection:** Dagger Hilt, Hilt Navigation Compose, Hilt Work
- **Persistence:** Room, DataStore Preferences
- **Networking:** Retrofit 2.11.0, Gson converter, OkHttp logging interceptor
- **Images:** Coil Compose, ExifInterface, custom bitmap storage helpers
- **Camera:** CameraX core, camera2, lifecycle, view, extensions
- **Widgets:** Jetpack Glance AppWidget and Glance Material 3
- **Notifications:** Firebase Cloud Messaging, Firebase Analytics, AlarmManager, WorkManager
- **Authentication & Security:** Credential Manager, Google Identity, BiometricPrompt
- **Health:** Health Connect Client
- **Location & Places:** Play Services Location, Google Places
- **Permissions:** Accompanist Permissions
- **Graphics:** Compose Canvas, graphicsLayer, native bitmap capture for sharing/export
- **Testing:** JUnit, AndroidX JUnit, Espresso, Compose UI Test

---

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer.
- JDK 17.
- Android SDK 35.
- Android device or emulator running API 26+.
- `google-services.json` in the `app/` directory for Firebase features.
- Backend API configured for the app's Retrofit network module.

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/ntlinhhhhhh/Moonpage.git
   ```

2. Add Firebase configuration:

   ```text
   app/google-services.json
   ```

3. Configure backend/API values in the network module if needed.

4. Sync Gradle and run the `:app` module from Android Studio.

### Useful Commands

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

On Windows:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

---

## Documentation

- [Architecture Guide](ARCHITECTURE.md)
- [API Specification](api.md)

---

## Project Notes

- Supported app resource languages are currently constrained to English and Vietnamese.
- Custom theme previews and shared diary images use local bitmap generation/storage.
- Widget data is backed by the local database and refreshed after key diary updates.

---

## License

This project is licensed under the MIT License.

