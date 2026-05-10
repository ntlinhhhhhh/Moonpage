package com.diary.moonpage.presentation.screens.store.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.core.theme.*

@Composable
fun CuteBeanIcon(
    modifier: Modifier = Modifier,
    emotion: String,
    decoration: String = "NONE",
    color: Color = Color(0xFFC5E1A5)
) {
    val drawableRes = when (emotion) {
        "VERY_HAPPY" -> com.diary.moonpage.R.drawable.very_happy
        "HAPPY" -> com.diary.moonpage.R.drawable.happy
        "NEUTRAL" -> com.diary.moonpage.R.drawable.neutral
        "SAD" -> com.diary.moonpage.R.drawable.sad
        "ANGRY" -> com.diary.moonpage.R.drawable.very_sad
        else -> com.diary.moonpage.R.drawable.neutral
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Vibrant Solid Background
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = color
        ) {}

        // Rich Decorations
        when (decoration) {
            "KITTY" -> {
                Canvas(modifier = Modifier.fillMaxSize().offset(x = (-10).dp, y = (-10).dp)) {
                    drawCircle(color = color, radius = size.minDimension / 4)
                }
                Canvas(modifier = Modifier.fillMaxSize().offset(x = 10.dp, y = (-10).dp)) {
                    drawCircle(color = color, radius = size.minDimension / 4)
                }
            }
            "SPROUT" -> {
                Canvas(modifier = Modifier.size(16.dp).offset(y = (-18).dp)) {
                    drawCircle(color = Color(0xFF81C784), radius = 4.dp.toPx())
                }
            }
            "BLUSHING" -> {
                Row(modifier = Modifier.width(26.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(modifier = Modifier.size(7.dp), shape = CircleShape, color = Color(0xFFFF8A80).copy(alpha = 0.7f)) {}
                    Surface(modifier = Modifier.size(7.dp), shape = CircleShape, color = Color(0xFFFF8A80).copy(alpha = 0.7f)) {}
                }
            }
            "PUPPY" -> {
                Canvas(modifier = Modifier.fillMaxSize().offset(x = (-12).dp, y = (-8).dp)) {
                    drawOval(color = color, size = Size(9.dp.toPx(), 18.dp.toPx()))
                }
                Canvas(modifier = Modifier.fillMaxSize().offset(x = 12.dp, y = (-8).dp)) {
                    drawOval(color = color, size = Size(9.dp.toPx(), 18.dp.toPx()))
                }
            }
            "HEART" -> {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(12.dp).offset(y = (-18).dp))
            }
            "WEATHER" -> {
                if (emotion == "VERY_HAPPY") {
                    Box(modifier = Modifier.size(10.dp).offset(x = 10.dp, y = (-10).dp).background(Color.Yellow, CircleShape))
                }
            }
            "COOKIE" -> {
                Box(modifier = Modifier.size(4.dp).offset(x = (-8).dp, y = (-8).dp).background(Color(0xFF3E2723), CircleShape))
                Box(modifier = Modifier.size(4.dp).offset(x = 8.dp, y = 8.dp).background(Color(0xFF3E2723), CircleShape))
            }
        }

        // Professional Black Facial Expression
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.55f) // Slightly smaller to reveal decorations better
        )
    }
}

@Composable
fun StoreTopBar(
    coins: Int,
    onBackClick: () -> Unit,
    onDoneClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = "Store",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (onDoneClick != null) {
            Button(
                onClick = onDoneClick,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .height(36.dp)
                    .align(Alignment.CenterEnd),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Activate",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .height(32.dp)
                    .align(Alignment.CenterEnd)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coins",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
@Composable
fun ThemeCard(
    theme: Theme,
    isSelected: Boolean = false,
    showSelectionIndicator: Boolean = true,
    onClick: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                val shades = getThemeShades(theme)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CuteBeanIcon(
                        modifier = Modifier.size(36.dp),
                        emotion = if (isSelected) "VERY_HAPPY" else "NEUTRAL",
                        decoration = theme.decoration,
                        color = shades.getOrElse(if (isSelected) 4 else 2) { Color.LightGray }
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = onSurface,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
                    )
                    Text(
                        text = theme.collection,
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurface.copy(alpha = 0.6f)
                    )
                }
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

        val shades = getThemeShades(theme)

        val defaultBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        val previewBg = if (theme.id == com.diary.moonpage.core.util.ThemeConstants.DEFAULT_THEME_ID) {
            Color(0xFFFFF2C2).copy(alpha = 0.2f)
        } else if (!theme.primaryColor.isNullOrBlank()) {
            try {
                val colorStr = if (theme.primaryColor.startsWith("#")) theme.primaryColor else "#${theme.primaryColor}"
                Color(android.graphics.Color.parseColor(colorStr)).copy(alpha = 0.15f)
            } catch (e: Exception) {
                defaultBg
            }
        } else {
            defaultBg
        }

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
                        modifier = Modifier.size(42.dp),
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
                val shades = getThemeShades(pack)
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
            val shades = getThemeShades(theme)
            Box(contentAlignment = Alignment.BottomEnd) {
                CuteBeanIcon(
                    modifier = Modifier.size(48.dp),
                    emotion = "VERY_HAPPY",
                    decoration = theme.decoration,
                    color = shades.getOrElse(4) { MaterialTheme.colorScheme.primary }
                )
                // Tick is removed as per user request to "bỏ hết các tick khi theme ấy activate đi"
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
            color = MoonTheme.customColors.popupBgColor,
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
                    val shades = getThemeShades(theme)
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
                            containerColor = MoonTheme.customColors.cancelBtnBgColor,
                            contentColor = MoonTheme.customColors.cancelBtnTextColor
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
                    Text("✓", color = MaterialTheme.colorScheme.primary, fontSize = 32.sp)
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
                text = "$themeName is now glowing in your archive with the warmth of the sun.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MoonTheme.customColors.popupBgColor,
        tonalElevation = 0.dp
    )
}

@Composable
fun ConfirmActivationDialog(
    themeName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MoonTheme.customColors.popupBgColor,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Confirm Activation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Do you want to set \"$themeName\" as your active theme?",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MoonTheme.customColors.cancelBtnBgColor,
                            contentColor = MoonTheme.customColors.cancelBtnTextColor
                        )
                    ) {
                        Text("No", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Activate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun getThemeShades(theme: Theme): List<Color> {
    val predefined = com.diary.moonpage.core.util.ThemeConstants.THEMES.find { it.id == theme.id }
    if (predefined != null) {
        return predefined.moods.map { mood ->
            try {
                Color(android.graphics.Color.parseColor(mood.iconUrl))
            } catch (e: Exception) {
                Color.LightGray
            }
        }
    }
    
    return when (theme.decoration) {
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
        "MOON" -> listOf(
            Color(0xFFFFF176), Color(0xFFFFEE58), Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFA000)
        )
        "AUTUMN" -> listOf(
            Color(0xFFFDF5E6), Color(0xFFF5DEB3), Color(0xFFDEB887), Color(0xFFE67E22), Color(0xFFD35400)
        )
        else -> listOf(
            Color(0xFFE8E1DA), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFF8D6E63), Color(0xFF5D4037)
        )
    }
}
