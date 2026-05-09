package com.diary.moonpage.presentation.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A section of avatars with a title
 */
@Composable
fun AvatarGroupSection(title: String, avatars: List<AvatarOption>) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            avatars.forEach { avatar ->
                AvatarOptionItem(avatar)
            }
        }
    }
}

/**
 * Individual avatar item
 */
@Composable
fun AvatarOptionItem(avatar: AvatarOption) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(avatar.color.copy(alpha = 0.2f), CircleShape)
            .clip(CircleShape)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(avatar.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    Box(modifier = Modifier.size(3.dp).background(Color.Black, CircleShape))
                    Spacer(modifier = Modifier.width(avatar.eyeSpacing.dp))
                    Box(modifier = Modifier.size(3.dp).background(Color.Black, CircleShape))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(avatar.mouthWidth.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Color.Black)
                )
            }
        }
    }
}
