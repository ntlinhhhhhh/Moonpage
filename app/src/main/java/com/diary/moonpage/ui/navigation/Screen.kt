package com.diary.moonpage.ui.navigation

sealed class Screen (val route: String) {
    // Auth Screens
    object Loading: Screen("loading_screen")
    object Tutorial: Screen("tutorial_screen")
    object Landing: Screen("landing_screen")
    object Login: Screen("login_screen")
    object Register: Screen("register_screen")
    object ForgotPassword: Screen("forgot_password_screen")
    object ResetPassword: Screen("reset_password_screen")
    object VerifyOtp: Screen("verify_otp_screen")
    object OnboardingBirthday: Screen("onboarding_birthday_screen")
    object OnboardingGender: Screen("onboarding_gender_screen")
    object OnboardingReminder: Screen("onboarding_reminder_screen")
    object ActivityCategorySelection: Screen("activity_category_selection_screen")

    // Main App Screens (Bottom Nav)
    object Calendar: Screen("calendar_screen")
    object Stats: Screen("stats_screen")
    object Camera: Screen("camera_screen?momentId={momentId}")
    object Store: Screen("store_screen")
    object Profile: Screen("profile_screen")

    // Profile Sub-screens
    object Account: Screen("account_screen")
    object Settings: Screen("settings_screen")
    object Notifications: Screen("notifications_screen")
    object Photos: Screen("photos_screen")
    object Gallery: Screen("gallery_screen")
    object ThemeCalendar: Screen("theme_calendar_screen")
    object Widgets: Screen("widgets_screen")
    object InviteFriend: Screen("invite_friend_screen")
    object StreakStats: Screen("streak_stats_screen")

    // Calendar Sub-screens
    object Filter: Screen("filter_screen")
    object DailyLog: Screen("daily_log_screen/{date}")
    object Music: Screen("music_screen")
    object MenstrualCycle: Screen("menstrual_cycle_screen")
    object DailyPhoto: Screen("daily_photo_screen")
    object ShareLog: Screen("share_log_screen/{date}")
    object ShareCalendar: Screen("share_calendar_screen/{yearMonth}")

    // Moment Sub-screens
    object MomentDetail: Screen("moment_detail_screen/{momentId}")

    // Security Screens
    object CreatePasscode: Screen("create_passcode_screen")
    object Lock: Screen("lock_screen")

    // Store Sub-screens
    object ThemeDetail: Screen("theme_detail_screen")

    // Stats Detail Screens
    object StatsMoodDetail: Screen("stats_mood_detail_screen")
    object StatsSleepDetail: Screen("stats_sleep_detail_screen")
    object StatsActivityDetail: Screen("stats_activity_detail_screen")
    object StatsInsightsDetail: Screen("stats_insights_detail_screen")
    object StatsMusicDetail: Screen("stats_music_detail_screen")
    
    // Annual Stats Detail Screens
    object StatsAnnualMoodDetail: Screen("stats_annual_mood_detail_screen")
    object StatsAnnualSleepDetail: Screen("stats_annual_sleep_detail_screen")
    object StatsAnnualActivityDetail: Screen("stats_annual_activity_detail_screen")
    object StatsAnnualBeansDetail: Screen("stats_annual_beans_detail_screen")
    object StatsAnnualMusicDetail: Screen("stats_annual_music_detail_screen")
}
