package com.diary.moonpage.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.moonpage.presentation.components.profile.AvatarOption
import com.diary.moonpage.presentation.components.profile.ProfileAvatarGroup
import com.diary.moonpage.presentation.theme.MoonPageTheme

/**
 * Stateful Screen for Changing Profile Picture
 */
@Composable
fun ChangeProfilePictureScreen(
    onNavigateBack: () -> Unit,
    onApply: () -> Unit
) {
    ChangeProfilePictureContent(
        onNavigateBack = onNavigateBack,
        onApply = onApply
    )
}

/**
 * Stateless Content for Changing Profile Picture
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeProfilePictureContent(
    onNavigateBack: () -> Unit,
    onApply: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val puppyAvatars = listOf(
        AvatarOption(1, Color(0xFFFDEFB1)),
        AvatarOption(2, Color(0xFFCDE8B5)),
        AvatarOption(3, Color(0xFF7DB97D)),
        AvatarOption(4, Color(0xFFB9D9A5))
    )

    val matchaAvatars = listOf(
        AvatarOption(5, Color(0xFFCDE8B5)),
        AvatarOption(6, Color(0xFFCDE8B5)),
        AvatarOption(7, Color(0xFF7DB97D)),
        AvatarOption(8, Color(0xFF7DB97D))
    )

    val heartAvatars = listOf(
        AvatarOption(9, Color(0xFFFFB3B3)),
        AvatarOption(10, Color(0xFFFF8080)),
        AvatarOption(11, Color(0xFFD35D5D)),
        AvatarOption(12, Color(0xFFA53D3D))
    )

    val basicAvatars = listOf(
        AvatarOption(13, Color(0xFFFDEFB1)),
        AvatarOption(14, Color(0xFFCDE8B5)),
        AvatarOption(15, Color(0xFF7DB97D)),
        AvatarOption(16, Color(0xFFB9D9A5))
    )

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Change profile picture", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp,
                        color = colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack, 
                            contentDescription = "Back",
                            tint = colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = colorScheme.background,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ProfileAvatarGroup("Puppy Bean", puppyAvatars)
            ProfileAvatarGroup("Daily Matcha Set", matchaAvatars)
            ProfileAvatarGroup("Heart Beans", heartAvatars)
            ProfileAvatarGroup("Basic Bean", basicAvatars)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangeProfilePicturePreview() {
    MoonPageTheme {
        ChangeProfilePictureContent({}, {})
    }
}
