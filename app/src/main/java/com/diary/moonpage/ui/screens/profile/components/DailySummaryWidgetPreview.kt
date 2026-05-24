package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diary.moonpage.R

private val ThemeDefaultPreviewSurface = Color(0xFFF4F6F1)
private val ThemeDefaultPreviewIcon = Color(0xFFDB9D1F)

@Composable
fun DailySummaryWidgetPreview(
    showStreak: Boolean,
    showNote: Boolean,
    showStats: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDefaultPreviewSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.very_happy),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp)
                )

                if (showNote) {
                    Text(
                        text = stringResource(R.string.daily_summary_preview_note),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                        maxLines = 2
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (showStats) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DailyPreviewStatItem(Icons.Rounded.DirectionsRun, "Activity", "Gym", Modifier.weight(1f))
                        DailyPreviewStatItem(Icons.Rounded.Bedtime, "Sleep", "7.5h", Modifier.weight(1f))
                        DailyPreviewStatItem(Icons.Rounded.DirectionsWalk, "Steps", "8.5k", Modifier.weight(1f))
                        DailyPreviewStatItem(Icons.Rounded.LocalFireDepartment, "Kcal", "2,150", Modifier.weight(1f))
                        DailyPreviewStatItem(Icons.Rounded.Route, "Dist", "5.2km", Modifier.weight(1f))
                    }
                }
            }

            if (showStreak) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(50),
                    color = Color(0xAA111111)
                ) {
                    Text(
                        text = "🔥 12",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyPreviewStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = ThemeDefaultPreviewIcon)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF888888), maxLines = 1)
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF333333), maxLines = 1)
    }
}
