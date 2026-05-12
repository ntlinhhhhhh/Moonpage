package com.diary.moonpage.presentation.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import com.diary.moonpage.presentation.components.core.feedback.MoonSnackbarHost
import com.diary.moonpage.presentation.components.profile.*
import com.diary.moonpage.core.theme.MoonPageTheme
import kotlinx.coroutines.launch

/**
 * BottomSheet type management
 */
enum class BottomSheetType { NONE, BIRTHDAY, GENDER, USERNAME }

/**
 * Stateful Screen for Account
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToChangeAvatar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val coroutineScope = rememberCoroutineScope()
    var currentBottomSheet by remember { mutableStateOf(BottomSheetType.NONE) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val user = uiState.user
    val context = LocalContext.current

    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateAvatar(context, it)
        }
    }

    // Fetch latest profile data when screen is launched
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    val hideBottomSheet = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                currentBottomSheet = BottomSheetType.NONE
            }
        }
    }

    // Listen for events (like showing snackbar)
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when(effect) {
                is ProfileUiEffect.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is ProfileUiEffect.UpdateSuccess -> {
                    // Success logic if needed
                }
                ProfileUiEffect.AccountDeleted -> {
                    onLogoutClick()
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogoutClick()
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AccountScreenContent(
            username = user?.name ?: "",
            gender = user?.gender ?: stringResource(R.string.not_specified),
            birthday = user?.birthday ?: stringResource(R.string.not_specified),
            userIdFull = user?.userId ?: "",
            email = user?.email ?: "",
            avatarUrl = user?.avatarUrl,
            localAvatarPath = uiState.localAvatarPath,
            tempAvatarPath = uiState.tempAvatarPath,
            onNavigateBack = onNavigateBack,
            onLogoutClick = { showLogoutDialog = true },
            onBirthdayClick = { currentBottomSheet = BottomSheetType.BIRTHDAY },
            onGenderClick = { currentBottomSheet = BottomSheetType.GENDER },
            onAvatarEditClick = { avatarLauncher.launch("image/*") },
            onUsernameEditClick = { currentBottomSheet = BottomSheetType.USERNAME },
            snackbarHostState = snackbarHostState
        )

        if (uiState.isUpdating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        MoonSnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
    }

    if (currentBottomSheet != BottomSheetType.NONE) {
        ModalBottomSheet(
            onDismissRequest = { currentBottomSheet = BottomSheetType.NONE },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets.ime.union(WindowInsets.navigationBars) },
            tonalElevation = 0.dp,
            scrimColor = Color.Black.copy(alpha = 0.32f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), CircleShape)
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                when (currentBottomSheet) {
                    BottomSheetType.GENDER -> {
                        GenderBottomSheetContent(
                            currentGender = user?.gender ?: "Other",
                            onGenderSelected = { newGender ->
                                viewModel.updateProfile(
                                    name = user?.name ?: "",
                                    gender = newGender,
                                    birthday = user?.birthday
                                )
                                hideBottomSheet()
                            },
                            onClose = { hideBottomSheet() }
                        )
                    }
                    BottomSheetType.BIRTHDAY -> {
                        BirthdayBottomSheetContent(
                            currentBirthday = user?.birthday ?: "01/01/2000",
                            onBirthdaySelected = { newBirthday ->
                                viewModel.updateProfile(
                                    name = user?.name ?: "",
                                    gender = user?.gender,
                                    birthday = newBirthday
                                )
                                hideBottomSheet()
                            },
                            onClose = { hideBottomSheet() }
                        )
                    }
                    BottomSheetType.USERNAME -> {
                        UsernameBottomSheetContent(
                            currentUsername = user?.name ?: "",
                            onUsernameChange = { newName ->
                                viewModel.updateProfile(
                                    name = newName,
                                    gender = user?.gender,
                                    birthday = user?.birthday
                                )
                                hideBottomSheet()
                            },
                            onClose = { hideBottomSheet() }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

/**
 * Logout Confirmation Dialog
 */
@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = com.diary.moonpage.core.theme.MoonTheme.customColors.popupBgColor,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = stringResource(R.string.log_out),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Main message
                Text(
                    text = stringResource(R.string.logout_confirmation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Warning message
                Text(
                    text = stringResource(R.string.logout_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.error,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnBgColor,
                            contentColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnTextColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Log out button
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error,
                            contentColor = colorScheme.onError
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            stringResource(R.string.log_out),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Stateless Content for Account Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreenContent(
    username: String,
    gender: String,
    birthday: String,
    userIdFull: String,
    email: String,
    avatarUrl: String? = null,
    localAvatarPath: String? = null,
    tempAvatarPath: String? = null,
    onNavigateBack: () -> Unit,
    onLogoutClick: () -> Unit,
    onBirthdayClick: () -> Unit,
    onGenderClick: () -> Unit,
    onAvatarEditClick: () -> Unit,
    onUsernameEditClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val colorScheme = MaterialTheme.colorScheme
    val isUsernameEmpty = username.trim().isEmpty()
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.account),
                            fontWeight = FontWeight.Bold,
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                AccountAvatar(
                    onEditClick = onAvatarEditClick,
                    avatarUrl = avatarUrl,
                    localAvatarPath = localAvatarPath,
                    tempAvatarPath = tempAvatarPath
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                Text(
                    text = if (isUsernameEmpty) stringResource(R.string.set_username) else username,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUsernameEmpty) colorScheme.onBackground.copy(alpha = 0.5f) else colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent, CircleShape)
                        .clickable { onUsernameEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit Username",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            AccountInfoRow(
                label = stringResource(R.string.user_id),
                value = userIdFull,
                actionText = stringResource(R.string.copy),
                icon = Icons.Rounded.Person,
                isColumnValue = true,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    clipboardManager.setText(AnnotatedString(userIdFull))
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.user_id_copied))
                    }
                }
            )

            AccountInfoRow(
                label = stringResource(R.string.birthday),
                value = birthday,
                showArrow = true,
                icon = Icons.Rounded.Cake,
                onClick = onBirthdayClick
            )

            AccountInfoRow(
                label = stringResource(R.string.gender),
                value = gender,
                showArrow = true,
                icon = Icons.Rounded.Wc,
                onClick = onGenderClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.login_information),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            AccountInfoRow(
                label = stringResource(R.string.my_social_account),
                value = email,
                iconRes = R.drawable.ic_google,
                isColumnValue = true,
                onClick = {}
            )

            AccountInfoRow(
                label = stringResource(R.string.change_social_account),
                value = "",
                icon = Icons.Rounded.Sync,
                showArrow = true,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(40.dp))

            TextButton(onClick = onLogoutClick) {
                Text(
                    stringResource(R.string.log_out),
                    color = colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
        }
        // Removed redundant MoonSnackbarHost as it's managed by the parent AccountScreen
    }
}
