package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diary.moonpage.R

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
            title = stringResource(R.string.posts),
            value = postsCount,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = stringResource(R.string.streak),
            value = streakCount,
            modifier = Modifier.weight(1f)
        )
    }
}
