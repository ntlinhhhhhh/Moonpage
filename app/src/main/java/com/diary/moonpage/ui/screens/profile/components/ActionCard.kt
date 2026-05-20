package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Rounded.PhotoLibrary,
    iconRes: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val iconColor = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(24.dp)

    Card(
        modifier = modifier
            .height(96.dp)
            .clip(shape)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textColor.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Navigate",
                    tint = textColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconBgColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconRes != null) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 28.sp
                    )
                )
            }
        }
    }
}
