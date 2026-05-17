package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AccountAvatar(
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    localAvatarPath: String? = null,
    tempAvatarPath: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val displayImage = tempAvatarPath ?: localAvatarPath ?: avatarUrl
            
            if (displayImage != null) {
                AsyncImage(
                    model = displayImage,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    // Smiley face using onPrimary color for contrast
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row {
                            Box(modifier = Modifier.size(4.dp).background(colorScheme.onPrimary, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(4.dp).background(colorScheme.onPrimary, CircleShape))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.width(12.dp).height(2.dp).background(colorScheme.onPrimary))
                    }
                }
            }
        }
        // Edit Icon on Avatar
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .size(32.dp)
                .offset(x = (-4).dp, y = (-4).dp)
                .background(colorScheme.onSurface.copy(alpha = 0.6f), CircleShape)
                .padding(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Edit Avatar",
                tint = colorScheme.surface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
