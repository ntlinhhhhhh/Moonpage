package com.diary.moonpage.ui.screens.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diary.moonpage.R
import com.diary.moonpage.ui.screens.profile.components.AvatarOption
import com.diary.moonpage.ui.screens.profile.components.ProfileAvatarGroup
import com.diary.moonpage.ui.screens.profile.components.ProfileAvatarItem
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.core.util.ComposeCaptureUtils
import java.io.File
import java.io.FileOutputStream

/**
 * Stateful Screen for Changing Profile Picture
 */
@Composable
fun ChangeProfilePictureScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onApply: () -> Unit
) {
    var selectedId by remember { mutableStateOf<Int?>(null) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current

    val puppyAvatars = remember { listOf(
        AvatarOption(1, Color(0xFFFDEFB1)),
        AvatarOption(2, Color(0xFFCDE8B5)),
        AvatarOption(3, Color(0xFF7DB97D)),
        AvatarOption(4, Color(0xFFB9D9A5))
    ) }

    val matchaAvatars = remember { listOf(
        AvatarOption(5, Color(0xFFCDE8B5)),
        AvatarOption(6, Color(0xFFCDE8B5)),
        AvatarOption(7, Color(0xFF7DB97D)),
        AvatarOption(8, Color(0xFF7DB97D))
    ) }

    val heartAvatars = remember { listOf(
        AvatarOption(9, Color(0xFFFFB3B3)),
        AvatarOption(10, Color(0xFFFF8080)),
        AvatarOption(11, Color(0xFFD35D5D)),
        AvatarOption(12, Color(0xFFA53D3D))
    ) }

    val basicAvatars = remember { listOf(
        AvatarOption(13, Color(0xFFFDEFB1)),
        AvatarOption(14, Color(0xFFCDE8B5)),
        AvatarOption(15, Color(0xFF7DB97D)),
        AvatarOption(16, Color(0xFFB9D9A5))
    ) }

    val allAvatars = remember { puppyAvatars + matchaAvatars + heartAvatars + basicAvatars }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateAvatar(context, it)
            onApply()
        }
    }

    ChangeProfilePictureContent(
        avatarUrl = uiState.user?.avatarUrl,
        localAvatarPath = uiState.localAvatarPath,
        tempAvatarPath = uiState.tempAvatarPath,
        onNavigateBack = onNavigateBack,
        selectedId = selectedId,
        onSelect = { selectedId = it },
        onPickFromGallery = { galleryLauncher.launch("image/*") },
        onApply = {
            val selectedAvatar = allAvatars.find { it.id == selectedId }
            if (selectedAvatar != null) {
                // Capture the avatar as a bitmap and update
                ComposeCaptureUtils.captureComposable(
                    view = view,
                    content = {
                        Box(modifier = Modifier.size(1080.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            ProfileAvatarItem(avatar = selectedAvatar, isSelected = false)
                        }
                    },
                    width = 1080,
                    height = 1080,
                    onBitmapCaptured = { bitmap ->
                        val file = File(context.cacheDir, "avatar_${selectedId}.png")
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        viewModel.updateAvatar(context, Uri.fromFile(file))
                        onApply()
                    }
                )
            } else {
                onApply()
            }
        },
        puppyAvatars = puppyAvatars,
        matchaAvatars = matchaAvatars,
        heartAvatars = heartAvatars,
        basicAvatars = basicAvatars
    )
}

/**
 * Stateless Content for Changing Profile Picture
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeProfilePictureContent(
    avatarUrl: String? = null,
    localAvatarPath: String? = null,
    tempAvatarPath: String? = null,
    onNavigateBack: () -> Unit,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    onPickFromGallery: () -> Unit,
    onApply: () -> Unit,
    puppyAvatars: List<AvatarOption>,
    matchaAvatars: List<AvatarOption>,
    heartAvatars: List<AvatarOption>,
    basicAvatars: List<AvatarOption>
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.change_profile_picture),
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp,
                        color = colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Rounded.ArrowBackIosNew, 
                            contentDescription = stringResource(R.string.back),
                            tint = colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onPickFromGallery) {
                        Icon(
                            Icons.Rounded.PhotoLibrary, 
                            contentDescription = stringResource(R.string.desc_pick_gallery),
                            tint = colorScheme.primary
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
                    enabled = selectedId != null,
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
                    Text(stringResource(R.string.apply), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                com.diary.moonpage.ui.screens.profile.components.AccountAvatar(
                    onEditClick = onPickFromGallery,
                    avatarUrl = avatarUrl,
                    localAvatarPath = localAvatarPath,
                    tempAvatarPath = tempAvatarPath
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            ProfileAvatarGroup("Puppy Bean", puppyAvatars, selectedId, onSelect)
            ProfileAvatarGroup("Daily Matcha Set", matchaAvatars, selectedId, onSelect)
            ProfileAvatarGroup("Heart Beans", heartAvatars, selectedId, onSelect)
            ProfileAvatarGroup("Basic Bean", basicAvatars, selectedId, onSelect)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangeProfilePicturePreview() {
    val avatars = listOf(AvatarOption(1, Color.Yellow))
    ChangeProfilePictureContent(
        avatarUrl = null,
        localAvatarPath = null,
        tempAvatarPath = null,
        onNavigateBack = {},
        selectedId = 1,
        onSelect = {},
        onPickFromGallery = {},
        onApply = {},
        puppyAvatars = avatars,
        matchaAvatars = avatars,
        heartAvatars = avatars,
        basicAvatars = avatars
    )
}
