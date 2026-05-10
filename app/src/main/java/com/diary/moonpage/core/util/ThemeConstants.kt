package com.diary.moonpage.core.util

data class PredefinedTheme(
    val id: String,
    val name: String,
    val price: Int,
    val thumbnailUrl: String,
    val backgroundUrl: String,
    val decoration: String,
    val moods: List<PredefinedMood>
)

data class PredefinedMood(
    val baseMoodId: String,
    val iconUrl: String,
    val customName: String
)

object ThemeConstants {
    const val DEFAULT_THEME_ID = "default_theme_id"

    val THEMES = listOf(
        PredefinedTheme(
            id = DEFAULT_THEME_ID,
            name = "Moon",
            price = 0,
            thumbnailUrl = "MoonActionLight",
            backgroundUrl = "MoonBgLight",
            decoration = "MOON",
            moods = listOf(
                PredefinedMood("Awful", "#FFF2C2", "Default mood 1"),
                PredefinedMood("Bad", "#FFE18A", "Default mood 2"),
                PredefinedMood("Meh", "#FFC547", "Default mood 3"),
                PredefinedMood("Good", "#DB9D1F", "Default mood 4"),
                PredefinedMood("Rad", "#A8730D", "Default mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_blushing",
            name = "Blushing",
            price = 100,
            thumbnailUrl = "#D2847A",
            backgroundUrl = "#FFF0F3",
            decoration = "BLUSHING",
            moods = listOf(
                PredefinedMood("Awful", "#FFC3BB", "Blushing mood 1"),
                PredefinedMood("Bad", "#FF9F98", "Blushing mood 2"),
                PredefinedMood("Meh", "#F07063", "Blushing mood 3"),
                PredefinedMood("Good", "#C24B42", "Blushing mood 4"),
                PredefinedMood("Rad", "#A03F38", "Blushing mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_kitty",
            name = "Kitty",
            price = 100,
            thumbnailUrl = "#8A9AFF",
            backgroundUrl = "#F0F3FF",
            decoration = "KITTY",
            moods = listOf(
                PredefinedMood("Awful", "#D6DFFF", "Kitty mood 1"),
                PredefinedMood("Bad", "#B3C2FF", "Kitty mood 2"),
                PredefinedMood("Meh", "#7A93FF", "Kitty mood 3"),
                PredefinedMood("Good", "#536FE6", "Kitty mood 4"),
                PredefinedMood("Rad", "#3B54BF", "Kitty mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_sprout",
            name = "Sprout",
            price = 100,
            thumbnailUrl = "#66BB6A",
            backgroundUrl = "#F1F8E9",
            decoration = "SPROUT",
            moods = listOf(
                PredefinedMood("Awful", "#D4F0D6", "Sprout mood 1"),
                PredefinedMood("Bad", "#AAE0AF", "Sprout mood 2"),
                PredefinedMood("Meh", "#6EC276", "Sprout mood 3"),
                PredefinedMood("Good", "#489E50", "Sprout mood 4"),
                PredefinedMood("Rad", "#307A37", "Sprout mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_midnight",
            name = "Midnight",
            price = 100,
            thumbnailUrl = "#1A1B26",
            backgroundUrl = "#E0E2EA",
            decoration = "MIDNIGHT",
            moods = listOf(
                PredefinedMood("Awful", "#FFF7D1", "Midnight mood 1"),
                PredefinedMood("Bad", "#F5E69A", "Midnight mood 2"),
                PredefinedMood("Meh", "#D4C059", "Midnight mood 3"),
                PredefinedMood("Good", "#A89532", "Midnight mood 4"),
                PredefinedMood("Rad", "#806F18", "Midnight mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_sunny",
            name = "Sunny",
            price = 100,
            thumbnailUrl = "#FFB300",
            backgroundUrl = "#FFF8E1",
            decoration = "SUNNY",
            moods = listOf(
                PredefinedMood("Awful", "#FFE6C2", "Sunny mood 1"),
                PredefinedMood("Bad", "#FFCD8F", "Sunny mood 2"),
                PredefinedMood("Meh", "#FAAA4B", "Sunny mood 3"),
                PredefinedMood("Good", "#D68322", "Sunny mood 4"),
                PredefinedMood("Rad", "#A86010", "Sunny mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_sky",
            name = "Sky",
            price = 100,
            thumbnailUrl = "#29B6F6",
            backgroundUrl = "#E1F5FE",
            decoration = "SKY",
            moods = listOf(
                PredefinedMood("Awful", "#D1F2FF", "Sky mood 1"),
                PredefinedMood("Bad", "#A3E5FF", "Sky mood 2"),
                PredefinedMood("Meh", "#5CCBFA", "Sky mood 3"),
                PredefinedMood("Good", "#34A6D6", "Sky mood 4"),
                PredefinedMood("Rad", "#1E82AB", "Sky mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_forest",
            name = "Forest",
            price = 100,
            thumbnailUrl = "#26A69A",
            backgroundUrl = "#E0F2F1",
            decoration = "FOREST",
            moods = listOf(
                PredefinedMood("Awful", "#D1EBE8", "Forest mood 1"),
                PredefinedMood("Bad", "#A8D9D4", "Forest mood 2"),
                PredefinedMood("Meh", "#6BB5AE", "Forest mood 3"),
                PredefinedMood("Good", "#44948D", "Forest mood 4"),
                PredefinedMood("Rad", "#2B736D", "Forest mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_coffee",
            name = "Coffee",
            price = 100,
            thumbnailUrl = "#8D6E63",
            backgroundUrl = "#EFEBE9",
            decoration = "COFFEE",
            moods = listOf(
                PredefinedMood("Awful", "#EBE2DD", "Coffee mood 1"),
                PredefinedMood("Bad", "#D6C6BC", "Coffee mood 2"),
                PredefinedMood("Meh", "#A68D81", "Coffee mood 3"),
                PredefinedMood("Good", "#826659", "Coffee mood 4"),
                PredefinedMood("Rad", "#61483D", "Coffee mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_lemon",
            name = "Lemon",
            price = 100,
            thumbnailUrl = "#CDDC39",
            backgroundUrl = "#F9FBE7",
            decoration = "LEMON",
            moods = listOf(
                PredefinedMood("Awful", "#F4FAD2", "Lemon mood 1"),
                PredefinedMood("Bad", "#E8F2A0", "Lemon mood 2"),
                PredefinedMood("Meh", "#C8D65A", "Lemon mood 3"),
                PredefinedMood("Good", "#A1AF35", "Lemon mood 4"),
                PredefinedMood("Rad", "#7B8721", "Lemon mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_cherry",
            name = "Cherry",
            price = 100,
            thumbnailUrl = "#EF5350",
            backgroundUrl = "#FFEBEE",
            decoration = "CHERRY",
            moods = listOf(
                PredefinedMood("Awful", "#FFD4D9", "Cherry mood 1"),
                PredefinedMood("Bad", "#FFA3AC", "Cherry mood 2"),
                PredefinedMood("Meh", "#EB606E", "Cherry mood 3"),
                PredefinedMood("Good", "#C43543", "Cherry mood 4"),
                PredefinedMood("Rad", "#991D29", "Cherry mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_lavender",
            name = "Lavender",
            price = 100,
            thumbnailUrl = "#AB47BC",
            backgroundUrl = "#F3E5F5",
            decoration = "LAVENDER",
            moods = listOf(
                PredefinedMood("Awful", "#F2DFFF", "Lavender mood 1"),
                PredefinedMood("Bad", "#E0B8FF", "Lavender mood 2"),
                PredefinedMood("Meh", "#B570EB", "Lavender mood 3"),
                PredefinedMood("Good", "#9147C9", "Lavender mood 4"),
                PredefinedMood("Rad", "#702C9E", "Lavender mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_ocean",
            name = "Ocean",
            price = 100,
            thumbnailUrl = "#42A5F5",
            backgroundUrl = "#E3F2FD",
            decoration = "OCEAN",
            moods = listOf(
                PredefinedMood("Awful", "#D6EBFF", "Ocean mood 1"),
                PredefinedMood("Bad", "#A8D3FF", "Ocean mood 2"),
                PredefinedMood("Meh", "#66AAEB", "Ocean mood 3"),
                PredefinedMood("Good", "#4083C4", "Ocean mood 4"),
                PredefinedMood("Rad", "#26629E", "Ocean mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_nebula",
            name = "Nebula",
            price = 100,
            thumbnailUrl = "#9C27B0",
            backgroundUrl = "#F3E5F5",
            decoration = "NEBULA",
            moods = listOf(
                PredefinedMood("Awful", "#F3E5F5", "Nebula mood 1"),
                PredefinedMood("Bad", "#E1BEE7", "Nebula mood 2"),
                PredefinedMood("Meh", "#BA68C8", "Nebula mood 3"),
                PredefinedMood("Good", "#9C27B0", "Nebula mood 4"),
                PredefinedMood("Rad", "#7B1FA2", "Nebula mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_matcha",
            name = "Matcha",
            price = 100,
            thumbnailUrl = "#4CAF50",
            backgroundUrl = "#E8F5E9",
            decoration = "MATCHA",
            moods = listOf(
                PredefinedMood("Awful", "#E8F5E9", "Matcha mood 1"),
                PredefinedMood("Bad", "#C8E6C9", "Matcha mood 2"),
                PredefinedMood("Meh", "#A5D6A7", "Matcha mood 3"),
                PredefinedMood("Good", "#81C784", "Matcha mood 4"),
                PredefinedMood("Rad", "#66BB6A", "Matcha mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_sunset",
            name = "Sunset",
            price = 100,
            thumbnailUrl = "#FF9800",
            backgroundUrl = "#FFF3E0",
            decoration = "SUNSET",
            moods = listOf(
                PredefinedMood("Awful", "#FFF3E0", "Sunset mood 1"),
                PredefinedMood("Bad", "#FFE0B2", "Sunset mood 2"),
                PredefinedMood("Meh", "#FFB74D", "Sunset mood 3"),
                PredefinedMood("Good", "#FFA726", "Sunset mood 4"),
                PredefinedMood("Rad", "#FF9800", "Sunset mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_galaxy",
            name = "Galaxy",
            price = 100,
            thumbnailUrl = "#3F51B5",
            backgroundUrl = "#E8EAF6",
            decoration = "GALAXY",
            moods = listOf(
                PredefinedMood("Awful", "#E8EAF6", "Galaxy mood 1"),
                PredefinedMood("Bad", "#C5CAE9", "Galaxy mood 2"),
                PredefinedMood("Meh", "#9FA8DA", "Galaxy mood 3"),
                PredefinedMood("Good", "#7986CB", "Galaxy mood 4"),
                PredefinedMood("Rad", "#5C6BC0", "Galaxy mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_autumn",
            name = "Autumn",
            price = 100,
            thumbnailUrl = "#D84315",
            backgroundUrl = "#FBE9E7",
            decoration = "AUTUMN",
            moods = listOf(
                PredefinedMood("Awful", "#FBE9E7", "Autumn mood 1"),
                PredefinedMood("Bad", "#FFCCBC", "Autumn mood 2"),
                PredefinedMood("Meh", "#FFAB91", "Autumn mood 3"),
                PredefinedMood("Good", "#E64A19", "Autumn mood 4"),
                PredefinedMood("Rad", "#BF360C", "Autumn mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_gray_brown",
            name = "Gray Brown",
            price = 100,
            thumbnailUrl = "#6D4C41",
            backgroundUrl = "#EFEBE9",
            decoration = "BROWN",
            moods = listOf(
                PredefinedMood("Awful", "#EFEBE9", "Gray Brown mood 1"),
                PredefinedMood("Bad", "#D7CCC8", "Gray Brown mood 2"),
                PredefinedMood("Meh", "#BCAAA4", "Gray Brown mood 3"),
                PredefinedMood("Good", "#8D6E63", "Gray Brown mood 4"),
                PredefinedMood("Rad", "#5D4037", "Gray Brown mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_cookie_batch",
            name = "Cookie Batch",
            price = 100,
            thumbnailUrl = "#8D6E63",
            backgroundUrl = "#FFF8E1",
            decoration = "COOKIE",
            moods = listOf(
                PredefinedMood("Awful", "#FFF8E1", "Cookie Batch mood 1"),
                PredefinedMood("Bad", "#FFECB3", "Cookie Batch mood 2"),
                PredefinedMood("Meh", "#FFD54F", "Cookie Batch mood 3"),
                PredefinedMood("Good", "#FFA000", "Cookie Batch mood 4"),
                PredefinedMood("Rad", "#8D6E63", "Cookie Batch mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_heart_felt",
            name = "Heart Felt",
            price = 100,
            thumbnailUrl = "#C2185B",
            backgroundUrl = "#FCE4EC",
            decoration = "HEART",
            moods = listOf(
                PredefinedMood("Awful", "#FCE4EC", "Heart Felt mood 1"),
                PredefinedMood("Bad", "#F8BBD0", "Heart Felt mood 2"),
                PredefinedMood("Meh", "#F06292", "Heart Felt mood 3"),
                PredefinedMood("Good", "#E91E63", "Heart Felt mood 4"),
                PredefinedMood("Rad", "#AD1457", "Heart Felt mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_weather_cycle",
            name = "Weather Cycle",
            price = 100,
            thumbnailUrl = "#455A64",
            backgroundUrl = "#ECEFF1",
            decoration = "WEATHER",
            moods = listOf(
                PredefinedMood("Awful", "#ECEFF1", "Weather Cycle mood 1"),
                PredefinedMood("Bad", "#CFD8DC", "Weather Cycle mood 2"),
                PredefinedMood("Meh", "#90A4AE", "Weather Cycle mood 3"),
                PredefinedMood("Good", "#607D8B", "Weather Cycle mood 4"),
                PredefinedMood("Rad", "#455A64", "Weather Cycle mood 5")
            )
        )
    )
}
