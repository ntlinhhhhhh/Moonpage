package com.diary.moonpage.presentation.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun YearHeader(
    year: String,
    userName: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = colorScheme.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = colorScheme.onBackground.copy(alpha = 0.3f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(year, fontSize = 48.sp, fontWeight = FontWeight.Black, color = accentColor)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(userName, fontSize = 16.sp)
                Text("'s Year", fontSize = 14.sp, color = accentColor, fontWeight = FontWeight.Bold)
            }
        }
        IconButton(onClick = onNextClick) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colorScheme.onBackground.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun MonthGridView(months: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(months) { month ->
            MonthItemCard(
                month = month,
                isSelected = month == "Apr",
                isCustom = month == "Mar"
            )
        }
    }
}

@Composable
fun MonthItemCard(month: String, isSelected: Boolean, isCustom: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    val bgColor = if (isSelected) colorScheme.secondary.copy(alpha = 0.2f) else colorScheme.surfaceVariant.copy(alpha = 0.5f)
    
    Card(
        modifier = Modifier
            .aspectRatio(1.0f)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isCustom) {
                CustomMonthDecoration(month)
            } else {
                DefaultMonthDecoration(month, isSelected)
            }
        }
    }
}

@Composable
fun DefaultMonthDecoration(month: String, isSelected: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(month, fontWeight = FontWeight.Bold, color = colorScheme.onBackground.copy(alpha = 0.8f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        SmileyIndicator(
            isSelected = isSelected,
            color = if (isSelected) colorScheme.primary.copy(alpha = 0.6f) else colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun CustomMonthDecoration(month: String) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(month, fontWeight = FontWeight.Bold, color = colorScheme.onBackground.copy(alpha = 0.8f), fontSize = 14.sp)
        Spacer(modifier = Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("6", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            Text("/ 31", fontSize = 12.sp, color = colorScheme.onBackground.copy(alpha = 0.5f))
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun SmileyIndicator(isSelected: Boolean, color: Color) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row {
                Box(modifier = Modifier.size(3.dp).background(contentColor, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(3.dp).background(contentColor, CircleShape))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(contentColor)
            )
        }
    }
}

@Composable
fun CalendarBottomActions(
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onDownloadClick,
            modifier = Modifier.weight(1f).height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.surfaceVariant,
                contentColor = colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Download", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onShareClick,
            modifier = Modifier.weight(1f).height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Share", fontWeight = FontWeight.Bold)
        }
    }
}
