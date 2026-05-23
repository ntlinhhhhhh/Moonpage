package com.diary.moonpage.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.moonpage.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCustomization: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val backText = stringResource(R.string.back)

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.widgets),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Icon(
                Icons.Rounded.Widgets,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                stringResource(R.string.widgets_moonpage_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                stringResource(R.string.widgets_moonpage_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            WidgetInstructionStep(
                stepNumber = 1,
                instruction = stringResource(R.string.widgets_instruction_1)
            )
            
            WidgetInstructionStep(
                stepNumber = 2,
                instruction = stringResource(R.string.widgets_instruction_2)
            )
            
            WidgetInstructionStep(
                stepNumber = 3,
                instruction = stringResource(R.string.widgets_instruction_3)
            )
            
            WidgetInstructionStep(
                stepNumber = 4,
                instruction = stringResource(R.string.widgets_instruction_4)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onNavigateToCustomization,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.got_it), modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun WidgetInstructionStep(
    stepNumber: Int,
    instruction: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stepNumber.toString(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            instruction,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
