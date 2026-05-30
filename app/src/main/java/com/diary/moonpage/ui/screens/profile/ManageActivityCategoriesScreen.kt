package com.diary.moonpage.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import com.diary.moonpage.core.util.MoonIcon
import com.diary.moonpage.ui.screens.auth.*

@Composable
fun ManageActivityCategoriesRoute(
    viewModel: ActivityCategoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ManageActivityCategoriesScreen(
        uiState = uiState,
        onToggleCategory = viewModel::toggle,
        onSelectAll = { viewModel.selectAll() },
        onDeselectAll = { viewModel.deselectAll() },
        onSave = {
            viewModel.save(onDone = onNavigateBack)
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageActivityCategoriesScreen(
    uiState: ActivityCategoryUiState,
    onToggleCategory: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val allKeys = ALL_ACTIVITY_CATEGORIES.map { it.key }.toSet()
    val isAllSelected = uiState.enabledCategories.containsAll(allKeys)

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = stringResource(R.string.back),
                        tint = colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.customize_blocks),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorScheme.onBackground
                )
                TextButton(
                    onClick = onSave,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text(
                        text = stringResource(R.string.customize_blocks_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { if (isAllSelected) onDeselectAll() else onSelectAll() },
                            enabled = !uiState.isLoading
                        ) {
                            Text(
                                text = if (isAllSelected) stringResource(R.string.deselect_all) else stringResource(R.string.select_all),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            items(ALL_ACTIVITY_CATEGORIES) { category ->
                val isSelected = category.key in uiState.enabledCategories
                ManageCategoryCard(
                    category = category,
                    isSelected = isSelected,
                    onClick = { onToggleCategory(category.key) }
                )
            }
        }
        
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ManageCategoryCard(
    category: ActivityCategoryInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.3f) else colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) colorScheme.primary else colorScheme.onBackground.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview icons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                category.previewIcons.take(3).forEach { moonIcon ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(moonIcon.color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (moonIcon.drawableRes != null) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = moonIcon.drawableRes),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        } else if (moonIcon.vector != null) {
                            Icon(
                                imageVector = moonIcon.vector,
                                contentDescription = null,
                                tint = moonIcon.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(category.displayNameRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }

            // Checkbox
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, colorScheme.onBackground.copy(alpha = 0.15f), CircleShape)
                    )
                }
            }
        }
    }
}
