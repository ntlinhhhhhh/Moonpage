package com.diary.moonpage.ui.screens.profile.components

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
 * Data class representing an avatar option (previously ProfileBean)
 */
data class AvatarOption(
    val id: Int,
    val color: Color,
    val eyeSpacing: Int = 8,
    val mouthWidth: Int = 12,
    val isSmiling: Boolean = true
)

/**
 * A section of avatars with a title (previously BeanSection)
 */
@Composable
fun ProfileAvatarGroup(
    title: String,
    avatars: List<AvatarOption>,
    selectedId: Int?,
    onSelect: (Int) -> Unit
) {
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
                ProfileAvatarItem(
                    avatar = avatar,
                    isSelected = selectedId == avatar.id,
                    onClick = { onSelect(avatar.id) }
                )
            }
        }
    }
}

/**
 * Individual avatar item (previously BeanItem)
 */
@Composable
fun ProfileAvatarItem(
    avatar: AvatarOption,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                if (isSelected) avatar.color.copy(alpha = 0.4f) else avatar.color.copy(alpha = 0.2f),
                CircleShape
            )
            .clip(CircleShape)
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.background(colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 56.dp else 50.dp)
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
