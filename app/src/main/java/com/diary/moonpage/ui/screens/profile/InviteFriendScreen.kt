package com.diary.moonpage.ui.screens.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.moonpage.R
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteFriendScreen(
    onNavigateBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val referralCode = "MOON-2026-HAPPY"
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val backText = stringResource(R.string.back)
    val copiedText = stringResource(R.string.invite_referral_copied)
    val shareSubject = stringResource(R.string.invite_share_subject)
    val shareText = stringResource(R.string.invite_share_text, referralCode)
    val chooserTitle = stringResource(R.string.invite_share_chooser_title)

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.invite_a_friend),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = backText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Icon(
                    Icons.Rounded.GroupAdd,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = colorScheme.primary.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    stringResource(R.string.invite_share_joy_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    stringResource(R.string.invite_share_joy_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    stringResource(R.string.invite_referral_code_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        referralCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    IconButton(onClick = { 
                        clipboardManager.setText(AnnotatedString(referralCode))
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(copiedText)
                        }
                    }) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = stringResource(R.string.copy), tint = colorScheme.primary)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text(stringResource(R.string.invite_share_invitation), style = MaterialTheme.typography.titleMedium)
                }
            }
            
            MoonSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
