package com.diary.moonpage.presentation.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileStatsBox(
    postsCount: String,
    streakCount: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Posts",
            value = postsCount,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Streak",
            value = streakCount,
            modifier = Modifier.weight(1f)
        )
    }
}
