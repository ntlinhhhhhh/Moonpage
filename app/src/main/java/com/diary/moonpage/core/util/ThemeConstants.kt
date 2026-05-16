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
                PredefinedMood("Rad", "#FFF2C2", "Default mood 1"),
                PredefinedMood("Good", "#FFE18A", "Default mood 2"),
                PredefinedMood("Meh", "#FFC547", "Default mood 3"),
                PredefinedMood("Bad", "#DB9D1F", "Default mood 4"),
                PredefinedMood("Awful", "#A8730D", "Default mood 5")
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
                PredefinedMood("Rad", "#FFC3BB", "Blushing mood 1"),
                PredefinedMood("Good", "#FF9F98", "Blushing mood 2"),
                PredefinedMood("Meh", "#F07063", "Blushing mood 3"),
                PredefinedMood("Bad", "#C24B42", "Blushing mood 4"),
                PredefinedMood("Awful", "#A03F38", "Blushing mood 5")
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
                PredefinedMood("Rad", "#D6DFFF", "Kitty mood 1"),
                PredefinedMood("Good", "#B3C2FF", "Kitty mood 2"),
                PredefinedMood("Meh", "#7A93FF", "Kitty mood 3"),
                PredefinedMood("Bad", "#536FE6", "Kitty mood 4"),
                PredefinedMood("Awful", "#3B54BF", "Kitty mood 5")
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
                PredefinedMood("Rad", "#D4F0D6", "Sprout mood 1"),
                PredefinedMood("Good", "#AAE0AF", "Sprout mood 2"),
                PredefinedMood("Meh", "#6EC276", "Sprout mood 3"),
                PredefinedMood("Bad", "#489E50", "Sprout mood 4"),
                PredefinedMood("Awful", "#307A37", "Sprout mood 5")
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
                PredefinedMood("Rad", "#FFF7D1", "Midnight mood 1"),
                PredefinedMood("Good", "#F5E69A", "Midnight mood 2"),
                PredefinedMood("Meh", "#D4C059", "Midnight mood 3"),
                PredefinedMood("Bad", "#A89532", "Midnight mood 4"),
                PredefinedMood("Awful", "#806F18", "Midnight mood 5")
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
                PredefinedMood("Rad", "#FFE6C2", "Sunny mood 1"),
                PredefinedMood("Good", "#FFCD8F", "Sunny mood 2"),
                PredefinedMood("Meh", "#FAAA4B", "Sunny mood 3"),
                PredefinedMood("Bad", "#D68322", "Sunny mood 4"),
                PredefinedMood("Awful", "#A86010", "Sunny mood 5")
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
                PredefinedMood("Rad", "#D1F2FF", "Sky mood 1"),
                PredefinedMood("Good", "#A3E5FF", "Sky mood 2"),
                PredefinedMood("Meh", "#5CCBFA", "Sky mood 3"),
                PredefinedMood("Bad", "#34A6D6", "Sky mood 4"),
                PredefinedMood("Awful", "#1E82AB", "Sky mood 5")
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
                PredefinedMood("Rad", "#D1EBE8", "Forest mood 1"),
                PredefinedMood("Good", "#A8D9D4", "Forest mood 2"),
                PredefinedMood("Meh", "#6BB5AE", "Forest mood 3"),
                PredefinedMood("Bad", "#44948D", "Forest mood 4"),
                PredefinedMood("Awful", "#2B736D", "Forest mood 5")
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
                PredefinedMood("Rad", "#EBE2DD", "Coffee mood 1"),
                PredefinedMood("Good", "#D6C6BC", "Coffee mood 2"),
                PredefinedMood("Meh", "#A68D81", "Coffee mood 3"),
                PredefinedMood("Bad", "#826659", "Coffee mood 4"),
                PredefinedMood("Awful", "#61483D", "Coffee mood 5")
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
                PredefinedMood("Rad", "#F4FAD2", "Lemon mood 1"),
                PredefinedMood("Good", "#E8F2A0", "Lemon mood 2"),
                PredefinedMood("Meh", "#C8D65A", "Lemon mood 3"),
                PredefinedMood("Bad", "#A1AF35", "Lemon mood 4"),
                PredefinedMood("Awful", "#7B8721", "Lemon mood 5")
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
                PredefinedMood("Rad", "#FFD4D9", "Cherry mood 1"),
                PredefinedMood("Good", "#FFA3AC", "Cherry mood 2"),
                PredefinedMood("Meh", "#EB606E", "Cherry mood 3"),
                PredefinedMood("Bad", "#C43543", "Cherry mood 4"),
                PredefinedMood("Awful", "#991D29", "Cherry mood 5")
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
                PredefinedMood("Rad", "#F2DFFF", "Lavender mood 1"),
                PredefinedMood("Good", "#E0B8FF", "Lavender mood 2"),
                PredefinedMood("Meh", "#B570EB", "Lavender mood 3"),
                PredefinedMood("Bad", "#9147C9", "Lavender mood 4"),
                PredefinedMood("Awful", "#702C9E", "Lavender mood 5")
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
                PredefinedMood("Rad", "#D6EBFF", "Ocean mood 1"),
                PredefinedMood("Good", "#A8D3FF", "Ocean mood 2"),
                PredefinedMood("Meh", "#66AAEB", "Ocean mood 3"),
                PredefinedMood("Bad", "#4083C4", "Ocean mood 4"),
                PredefinedMood("Awful", "#26629E", "Ocean mood 5")
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
                PredefinedMood("Rad", "#F3E5F5", "Nebula mood 1"),
                PredefinedMood("Good", "#E1BEE7", "Nebula mood 2"),
                PredefinedMood("Meh", "#BA68C8", "Nebula mood 3"),
                PredefinedMood("Bad", "#9C27B0", "Nebula mood 4"),
                PredefinedMood("Awful", "#7B1FA2", "Nebula mood 5")
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
                PredefinedMood("Rad", "#E8F5E9", "Matcha mood 1"),
                PredefinedMood("Good", "#C8E6C9", "Matcha mood 2"),
                PredefinedMood("Meh", "#A5D6A7", "Matcha mood 3"),
                PredefinedMood("Bad", "#81C784", "Matcha mood 4"),
                PredefinedMood("Awful", "#66BB6A", "Matcha mood 5")
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
                PredefinedMood("Rad", "#FFF3E0", "Sunset mood 1"),
                PredefinedMood("Good", "#FFE0B2", "Sunset mood 2"),
                PredefinedMood("Meh", "#FFB74D", "Sunset mood 3"),
                PredefinedMood("Bad", "#FFA726", "Sunset mood 4"),
                PredefinedMood("Awful", "#FF9800", "Sunset mood 5")
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
                PredefinedMood("Rad", "#E8EAF6", "Galaxy mood 1"),
                PredefinedMood("Good", "#C5CAE9", "Galaxy mood 2"),
                PredefinedMood("Meh", "#9FA8DA", "Galaxy mood 3"),
                PredefinedMood("Bad", "#7986CB", "Galaxy mood 4"),
                PredefinedMood("Awful", "#5C6BC0", "Galaxy mood 5")
            )
        ),
        PredefinedTheme(
            id = "theme_autumn",
            name = "Autumn",
            price = 100,
            thumbnailUrl = "#E67E22",
            backgroundUrl = "#FDF5E6",
            decoration = "AUTUMN",
            moods = listOf(
                PredefinedMood("Rad", "#FDF5E6", "Autumn mood 1"),
                PredefinedMood("Good", "#F5DEB3", "Autumn mood 2"),
                PredefinedMood("Meh", "#DEB887", "Autumn mood 3"),
                PredefinedMood("Bad", "#CD853F", "Autumn mood 4"),
                PredefinedMood("Awful", "#8B4513", "Autumn mood 5")
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
                PredefinedMood("Rad", "#EFEBE9", "Gray Brown mood 1"),
                PredefinedMood("Good", "#D7CCC8", "Gray Brown mood 2"),
                PredefinedMood("Meh", "#BCAAA4", "Gray Brown mood 3"),
                PredefinedMood("Bad", "#8D6E63", "Gray Brown mood 4"),
                PredefinedMood("Awful", "#5D4037", "Gray Brown mood 5")
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
                PredefinedMood("Rad", "#FFF8E1", "Cookie Batch mood 1"),
                PredefinedMood("Good", "#FFECB3", "Cookie Batch mood 2"),
                PredefinedMood("Meh", "#FFD54F", "Cookie Batch mood 3"),
                PredefinedMood("Bad", "#FFA000", "Cookie Batch mood 4"),
                PredefinedMood("Awful", "#8D6E63", "Cookie Batch mood 5")
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
                PredefinedMood("Rad", "#FCE4EC", "Heart Felt mood 1"),
                PredefinedMood("Good", "#F8BBD0", "Heart Felt mood 2"),
                PredefinedMood("Meh", "#F06292", "Heart Felt mood 3"),
                PredefinedMood("Bad", "#E91E63", "Heart Felt mood 4"),
                PredefinedMood("Awful", "#AD1457", "Heart Felt mood 5")
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
                PredefinedMood("Rad", "#ECEFF1", "Weather Cycle mood 1"),
                PredefinedMood("Good", "#CFD8DC", "Weather Cycle mood 2"),
                PredefinedMood("Meh", "#90A4AE", "Weather Cycle mood 3"),
                PredefinedMood("Bad", "#607D8B", "Weather Cycle mood 4"),
                PredefinedMood("Awful", "#455A64", "Weather Cycle mood 5")
            )
        )
    )
}
