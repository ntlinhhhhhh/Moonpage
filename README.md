# 🌙 Moon Page - Your Personal Soul Diary

[![Build Status](https://img.shields.io/badge/Build-Success-brightgreen.svg)]()
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-2024.04-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/compose)

**Moon Page** is a modern, aesthetically pleasing diary application designed to help users track their moods, activities, and precious moments. With a focus on emotional well-being and intuitive design, Moon Page transforms daily logging into a delightful ritual.

---

## Key Features

### 📅 Intuitive Calendar Logging
- **Daily Journaling:** Log your thoughts, activities, and feelings with a few taps.
- **Mood Tracking:** Choose from 5 expressive mood levels with custom "Cute Beans" icons.
- **Activity Enrichment:** Tag activities (Exercise, Reading, Gaming, etc.) to see how they impact your mood.
- **Menstrual Cycle Tracking:** Integrated cycle logging for comprehensive health monitoring.

### 📸 Moments & Memories
- **In-App Camera:** Capture the essence of your day directly within the app.
- **Moment Gallery:** Revisit your past photos and memories in a beautiful, organized gallery.
- **Public/Private Moments:** Choose whether to keep your moments private or share them with the community.

### 📊 Insightful Statistics & Recap
- **Mood Distribution:** Detailed charts showing your emotional patterns over months and years.
- **Activity Analysis (Best/Worst):** Advanced correlation algorithms to identify habits that impact your mood positively or negatively.
- **Year in Pixels & Annual Recap:** A shareable, visually stunning 365-day grid summarizing your entire year's emotional journey.
- **Streak System:** Stay motivated with daily login streaks and "Streak Freeze" items.

### 🔔 Smart Notification System
- **Real-time Push Notifications:** Integrated with Firebase Cloud Messaging (FCM) to deliver and permanently record system messages, streaks, and updates.
- **Reliable Local Reminders:** Punctual daily journaling reminders powered by Android's Exact Alarms (`setExactAndAllowWhileIdle`) and self-rescheduling receivers.
- **In-App Notification Bus:** Seamless real-time Snackbars and automatic UI refreshing without interrupting the user experience.

### 🎨 Personalization & Store
- **Premium Themes:** Unlock unique UI skins and icon packs (Blushing, Midnight, Forest, etc.) using in-app "Moon Coins."
- **Dark/Light Mode:** Seamless support for System, Light, and Dark appearance.
- **Dynamic Theming:** UI elements adapt their color palette based on your active theme.

### 🔒 Security & Integrations
- **Biometric Lock:** Secure your private diary with Fingerprint or Face ID.
- **Spotify Integration:** Automatically attach recently played or top tracks to your logs, intelligently adapting to Spotify Free or Premium accounts.
- **Health Connect API:** Deep integration to seamlessly aggregate your daily steps, total calories burned, distance, and detailed sleep analysis (Bedtime & Wake-up averages).

---

## 🏗Architecture

Moon Page is built following **Clean Architecture** principles and the **MVVM** (Model-View-ViewModel) pattern. This ensuring the codebase is scalable, testable, and maintainable.

### Layers:
1.  **`core`**: Cross-cutting concerns including DI (Hilt), network configurations, and shared utilities.
2.  **`domain`**: Pure business logic (UseCases, Repositories Interfaces, and Models). No Android dependencies.
3.  **`data`**: Implementation of repositories, local database (Room), and remote API services (Retrofit).
4.  **`presentation`**: UI layer powered by Jetpack Compose, handling state management with ViewModels and StateFlow.

---

## Tech Stack & Frameworks

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI:** [Jetpack Compose](https://developer.android.com/compose) (Modern Android Toolkit)
  - *Advanced Graphics:* `GraphicsLayer` and Canvas capture for rendering and sharing Composable bitmaps natively.
- **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)
- **Database:** [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Networking:** [Retrofit 2](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **Third-Party APIs:**
  - **Spotify Web API:** OAuth 2.0 (PKCE) integration, handling Free vs. Premium account endpoints (`/me/top/tracks` vs `/me/player/recently-played`).
- **Health & Fitness:** [Health Connect API](https://developer.android.com/guide/health-and-fitness/health-connect) (Aggregate Data fetching for Steps, Calories, Distance, and Sleep Session analysis).
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/)
- **Asynchronous Flow:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) (Dispatchers.IO for background processing) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Navigation:** [Jetpack Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- **Local Storage:** [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) (Typed, asynchronous storage for settings and onboarding flags).
- **Notifications & Background Tasks:** 
  - [Firebase Cloud Messaging (FCM)](https://firebase.google.com/docs/cloud-messaging) for real-time Push Notifications.
  - `AlarmManager` with `setExactAndAllowWhileIdle` for punctual local daily reminders.
  - Custom `NotificationBus` using `SharedFlow` for real-time in-app Snackbar alerts.
- **Camera:** [CameraX](https://developer.android.com/training/camerax)
- **Security:** 
  - [Credential Manager](https://developer.android.com/training/sign-in/credential-manager) for Google One Tap Sign-In.
  - `BiometricPrompt` for local Face ID/Fingerprint app lock.

---

## Getting Started

### Prerequisites
- Android Studio Ladybug | 2024.2.1 or newer.
- Android SDK 35 (Compile SDK).
- A physical Android device or Emulator (API level 26+).

### Installation
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/ntlinhhhhhh/Moonpage.git
    ```
2.  **Set up Firebase:**
    - Place your `google-services.json` file in the `app/` directory.
3.  **API Configuration:**
    - Update your base URL and API keys in the `core/di/NetworkModule.kt` if necessary.
4.  **Build and Run:**
    - Sync Gradle and run the `:app` module on your device.

---

## Documentation

- [Detailed Architecture Guide](ARCHITECTURE.md) - Deep dive into our implementation of Clean Architecture.
- [API Specification](api.md) - Overview of Backend endpoints and data structures.

---

## Contributing

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

*Moon Page - Logging your soul, one bean at a time.* 🌙
