package com.diary.moonpage.presentation.screens.store.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.core.theme.*

@Composable
fun CuteBeanIcon(
    modifier: Modifier = Modifier,
    emotion: String,
    decoration: String = "NONE",
    color: Color = Color(0xFFC5E1A5)
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = color
        ) {}

        when (decoration) {
            "KITTY" -> {
                Canvas(modifier = Modifier.size(40.dp).offset(x = (-12).dp, y = (-12).dp)) {
                    drawCircle(color = color, radius = size.minDimension / 4)
                }
                Canvas(modifier = Modifier.size(40.dp).offset(x = 12.dp, y = (-12).dp)) {
                    drawCircle(color = color, radius = size.minDimension / 4)
                }
            }
            "SPROUT" -> {
                Canvas(modifier = Modifier.size(20.dp).offset(y = (-20).dp)) {
                    drawCircle(color = Color(0xFF81C784), radius = 4.dp.toPx())
                }
            }
            "BLUSHING" -> {
                Row(modifier = Modifier.width(30.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = Color(0xFFFF8A80).copy(alpha = 0.6f)) {}
                    Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = Color(0xFFFF8A80).copy(alpha = 0.6f)) {}
                }
            }
            "PUPPY" -> {
                Canvas(modifier = Modifier.size(40.dp).offset(x = (-14).dp, y = (-8).dp)) {
                    drawOval(color = color, size = Size(10.dp.toPx(), 20.dp.toPx()))
                }
                Canvas(modifier = Modifier.size(40.dp).offset(x = 14.dp, y = (-8).dp)) {
                    drawOval(color = color, size = Size(10.dp.toPx(), 20.dp.toPx()))
                }
            }
            "MUSHROOM" -> {
                Canvas(modifier = Modifier.size(40.dp).offset(y = (-16).dp)) {
                    drawArc(color = Color.Red.copy(alpha = 0.8f), startAngle = 180f, sweepAngle = 180f, useCenter = true)
                }
            }
            "COOKIE" -> {
                repeat(4) { 
                    Box(modifier = Modifier.size(2.dp).offset(x = (it * 4).dp, y = (it % 2).dp).background(Color(0xFF3E2723), CircleShape))
                }
            }
            "HEART" -> {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(12.dp).offset(y = (-18).dp))
            }
            "WEATHER" -> {
                if (emotion == "VERY_HAPPY") {
                    Box(modifier = Modifier.size(12.dp).offset(x = 12.dp, y = (-12).dp).background(Color.Yellow, CircleShape))
                } else if (emotion == "ANGRY") {
                    Box(modifier = Modifier.size(12.dp).offset(x = 12.dp, y = (-12).dp).background(Color.Gray, CircleShape))
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.width(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                if (emotion == "ANGRY") {
                    Canvas(modifier = Modifier.size(4.dp)) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, size.height)
                        }
                        drawPath(path, Color.Black, style = Stroke(width = 2.dp.toPx()))
                    }
                    Canvas(modifier = Modifier.size(4.dp)) {
                        val path = Path().apply {
                            moveTo(size.width, 0f)
                            lineTo(0f, size.height)
                        }
                        drawPath(path, Color.Black, style = Stroke(width = 2.dp.toPx()))
                    }
                } else {
                    Box(modifier = Modifier.size(3.dp).background(Color.Black, CircleShape))
                    Box(modifier = Modifier.size(3.dp).background(Color.Black, CircleShape))
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            when (emotion) {
                "VERY_HAPPY" -> Box(modifier = Modifier.size(width = 10.dp, height = 5.dp).background(Color.Black, RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp)))
                "HAPPY" -> Box(modifier = Modifier.size(width = 6.dp, height = 2.dp).background(Color.Black, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)))
                "NEUTRAL" -> Box(modifier = Modifier.size(width = 6.dp, height = 1.dp).background(Color.Black))
                "SAD" -> Box(modifier = Modifier.size(width = 6.dp, height = 3.dp).background(Color.Black, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)))
                "ANGRY" -> Box(modifier = Modifier.size(width = 6.dp, height = 2.dp).background(Color.Black, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)))
            }
        }
    }
}

@Composable
fun StoreTopBar(
    coins: Int,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Store",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .height(32.dp)
                .align(Alignment.CenterEnd)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$",
                        modifier = Modifier.offset(y = (-0.8).dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$coins Coins",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: Theme,
    onClick: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = theme.collection,
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurface.copy(alpha = 0.6f)
                )
            }

            if (theme.isOwned) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Purchased",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$", 
                                modifier = Modifier.offset(y = (-0.8).dp),
                                color = MaterialTheme.colorScheme.onPrimary, 
                                fontSize = 8.sp, 
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${theme.price}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val shades = if (theme.decoration == "MOON") {
            listOf(Color(0xFFFFF176), Color(0xFFFFEE58), Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFA000))
        } else {
            getThemeShades(theme.decoration)
        }

        val isNewStyle = listOf("SUNNY", "SKY", "FOREST", "COFFEE", "LEMON", "CHERRY", "LAVENDER", "OCEAN", "SPROUT", "NEBULA", "MATCHA", "SUNSET", "GALAXY", "AUTUMN", "GRAY_BROWN", "COOKIE_BATCH", "HEART_FELT", "WEATHER_CYCLE").contains(theme.decoration)
        val previewBg = if (theme.primaryColor != null) Color(android.graphics.Color.parseColor(theme.primaryColor)).copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(previewBg),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                theme.icons.forEachIndexed { index, emotion ->
                    CuteBeanIcon(
                        emotion = emotion,
                        decoration = theme.decoration,
                        color = shades.getOrElse(index) { Color.LightGray }
                    )
                }
            }
        }
    }
}

@Composable
fun IconPackCard(pack: Theme, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val shades = if (pack.decoration == "MOON") {
                    listOf(
                        Color(0xFFFFF176), Color(0xFFFFEE58), Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFA000)
                    )
                } else {
                    getThemeShades(pack.decoration)
                }
                pack.icons.take(2).forEachIndexed { index, emotion ->
                    CuteBeanIcon(
                        modifier = Modifier.size(24.dp),
                        emotion = emotion,
                        decoration = pack.decoration,
                        color = shades.getOrElse(index) { Color.LightGray }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pack.name, 
                style = MaterialTheme.typography.labelLarge, 
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (pack.isOwned) {
                Text(
                    text = "Purchased", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$",
                            modifier = Modifier.offset(y = (-0.8).dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${pack.price}", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreMoreCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add, 
                    contentDescription = null, 
                    modifier = Modifier.padding(8.dp), 
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Explore More", 
                style = MaterialTheme.typography.labelLarge, 
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CurrentThemeCard(theme: Theme) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape), 
                contentAlignment = Alignment.Center
            ) {
                Text(text = "âœ“", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = theme.name, 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Active since Oct 24, 2023", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ConfirmPurchaseDialog(
    theme: Theme,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = com.diary.moonpage.core.theme.MoonTheme.customColors.popupBgColor,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Confirm Purchase",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Are you sure you want to buy ${theme.name}?",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val shades = if (theme.decoration == "MOON") {
                        listOf(
                            Color(0xFFFFF176), Color(0xFFFFEE58), Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFA000)
                        )
                    } else {
                        getThemeShades(theme.decoration)
                    }
                    theme.icons.forEachIndexed { index, emotion ->
                        CuteBeanIcon(
                            modifier = Modifier.size(36.dp),
                            emotion = emotion,
                            decoration = theme.decoration,
                            color = shades.getOrElse(index) { Color.LightGray }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnBgColor,
                            contentColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnTextColor
                        )
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Proceed")
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseSuccessDialog(
    themeName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Continue")
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("âœ“", color = MaterialTheme.colorScheme.primary, fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Purchased", 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Text(
                text = "Your archive is now glowing with the warmth of the sun.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.popupBgColor,
        tonalElevation = 0.dp
    )
}

fun getThemeShades(decoration: String): List<Color> {
    return when (decoration) {
        "BLUSHING" -> listOf(
            Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFEF9A9A), Color(0xFFE57373), Color(0xFFEF5350)
        )
        "KITTY" -> listOf(
            Color(0xFFE8EAF6), Color(0xFFC5CAE9), Color(0xFF9FA8DA), Color(0xFF7986CB), Color(0xFF5C6BC0)
        )
        "SPROUT" -> listOf(
            Color(0xFFF1F8E9), Color(0xFFDCEDC8), Color(0xFFC5E1A5), Color(0xFFAED581), Color(0xFF9CCC65)
        )
        "SUNNY" -> listOf(
            Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFD54F), Color(0xFFFFCA28), Color(0xFFFFB300)
        )
        "SKY" -> listOf(
            Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF81D4FA), Color(0xFF4FC3F7), Color(0xFF29B6F6)
        )
        "FOREST" -> listOf(
            Color(0xFFE0F2F1), Color(0xFFB2DFDB), Color(0xFF80CBC4), Color(0xFF4DB6AC), Color(0xFF26A69A)
        )
        "COFFEE" -> listOf(
            Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFF8D6E63), Color(0xFF6D4C41)
        )
        "LEMON" -> listOf(
            Color(0xFFF9FBE7), Color(0xFFF0F4C3), Color(0xFFE6EE9C), Color(0xFFDCE775), Color(0xFFCDDC39)
        )
        "CHERRY" -> listOf(
            Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFEF9A9A), Color(0xFFE57373), Color(0xFFF44336)
        )
        "LAVENDER" -> listOf(
            Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFCE93D8), Color(0xFFBA68C8), Color(0xFFAB47BC)
        )
        "OCEAN" -> listOf(
            Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFF90CAF9), Color(0xFF64B5F6), Color(0xFF2196F3)
        )
        "BROWN" -> listOf(
            Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFF8D6E63), Color(0xFF5D4037)
        )
        "COOKIE" -> listOf(
            Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFFCC80), Color(0xFFFFB74D), Color(0xFFFFA726)
        )
        "HEART" -> listOf(
            Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFCE93D8), Color(0xFFBA68C8), Color(0xFFAB47BC)
        )
        "WEATHER" -> listOf(
            Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF81D4FA), Color(0xFF4FC3F7), Color(0xFF29B6F6)
        )
        else -> listOf(
            Color(0xFFE8E1DA), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFF8D6E63), Color(0xFF5D4037)
        )
    }
}
