package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diary.moonpage.R

@Composable
fun UserInfoCard(
    userId: String,
    userName: String,
    avatarUrl: String?,
    localAvatarPath: String? = null,
    tempAvatarPath: String? = null,
    onClick: () -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val outerCircleColor = MaterialTheme.colorScheme.surfaceVariant
    val innerCircleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)

    val shape = RoundedCornerShape(20.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with outer ring and smiley bean or AsyncImage
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(outerCircleColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val displayImage = tempAvatarPath ?: localAvatarPath ?: avatarUrl

                if (displayImage != null) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = stringResource(R.string.content_desc_avatar),
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(innerCircleColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row {
                                Box(modifier = Modifier.size(3.dp).background(Color.Black, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.size(3.dp).background(Color.Black, CircleShape))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Canvas(modifier = Modifier.size(width = 12.dp, height = 6.dp)) {
                                drawArc(
                                    color = Color.Black,
                                    startAngle = 0f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = userId,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColor.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = stringResource(R.string.content_desc_navigate),
                tint = textColor.copy(alpha = 0.3f)
            )
        }
    }
}


