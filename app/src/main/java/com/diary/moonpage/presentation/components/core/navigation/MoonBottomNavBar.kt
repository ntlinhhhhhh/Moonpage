package com.diary.moonpage.presentation.components.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diary.moonpage.presentation.navigation.Screen
import com.diary.moonpage.presentation.theme.*

@Composable
fun MoonBottomNavBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val navBgColor = MaterialTheme.colorScheme.surface
    val cameraBgColor = MaterialTheme.colorScheme.background
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)

    val calendar = Screen.Calendar.route
    val stats = Screen.Stats.route
    val camera = Screen.Camera.route
    val store = Screen.Store.route
    val profile = Screen.Profile.route

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, spotColor = Color.Black.copy(alpha = 0.1f)),
        color = navBgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            NavBarItem(
                icon = Icons.Rounded.CalendarMonth,
                label = calendar,
                isSelected = selectedRoute == calendar,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onItemSelected(calendar) }
            )

            NavBarItem(
                icon = Icons.Rounded.BarChart,
                label = stats,
                isSelected = selectedRoute == stats,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onItemSelected(stats) }
            )

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(cameraBgColor)
                    .clickable { onItemSelected(camera) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "Camera",
                    tint = if (selectedRoute == camera) activeColor else inactiveColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            NavBarItem(
                icon = Icons.Rounded.Storefront,
                label = store,
                isSelected = selectedRoute == store,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onItemSelected(store) }
            )

            NavBarItem(
                icon = Icons.Rounded.Person,
                label = profile,
                isSelected = selectedRoute == profile,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onItemSelected(profile) }
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val color = if (isSelected) activeColor else inactiveColor
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF4)
@Composable
fun BottomNavPreviewLight() {
    MoonPageTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            MoonBottomNavBar(selectedRoute = Screen.Profile.route, onItemSelected = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333746)
@Composable
fun BottomNavPreviewDark() {
    MoonPageTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            MoonBottomNavBar(selectedRoute = Screen.Profile.route, onItemSelected = {})
        }
    }
}
