package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.YearMonth
import com.diary.moonpage.R
import com.diary.moonpage.ui.components.inputs.MoonTextField
import com.diary.moonpage.core.util.UiText
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.focus.FocusDirection

private const val INFINITE_MULTIPLIER = 1000

private val YEAR_LIST = (1950..java.time.LocalDate.now().year).map { it.toString() }

@Composable
fun BottomSheetHeader(title: String, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close, 
                contentDescription = stringResource(R.string.close),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun UsernameBottomSheetContent(
    currentUsername: String,
    onUsernameChange: (String) -> Unit,
    onClose: () -> Unit
) {
    var text by remember { mutableStateOf(currentUsername) }
    val maxLength = 20
    val colorScheme = MaterialTheme.colorScheme
    val isChanged = text != currentUsername && text.isNotBlank()

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 24.dp)
    ) {
        BottomSheetHeader(title = stringResource(R.string.change_username), onClose = onClose)

        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= maxLength) text = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.1f),
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface
            ),
            singleLine = true
        )

        Text(
            text = "${text.length}/$maxLength",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (isChanged) {
                    onUsernameChange(text)
                    onClose()
                }
            },
            enabled = isChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                disabledContainerColor = colorScheme.primary.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(stringResource(R.string.change), color = if (isChanged) colorScheme.onPrimary else colorScheme.onPrimary.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun GenderBottomSheetContent(
    currentGender: String,
    onGenderSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val options = listOf(
        "Female" to stringResource(R.string.gender_female),
        "Male" to stringResource(R.string.gender_male),
        "Other" to stringResource(R.string.gender_other)
    )
    var selectedOption by remember { mutableStateOf(if (currentGender.isBlank()) "Female" else currentGender) }
    val colorScheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .padding(bottom = 32.dp)
    ) {
        BottomSheetHeader(title = stringResource(R.string.gender), onClose = onClose)

        Spacer(modifier = Modifier.height(8.dp))

        options.forEach { (value, label) ->
            val isSelected = value.equals(selectedOption, ignoreCase = true)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) colorScheme.primary.copy(alpha = 0.1f)
                        else colorScheme.surface
                    )
                    .clickable { 
                        selectedOption = value
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.45f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onGenderSelected(selectedOption)
                onClose()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(stringResource(R.string.done), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun BirthdayBottomSheetContent(
    currentBirthday: String,
    onBirthdaySelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Parse current birthday
    val parts = currentBirthday.split("/", "-")
    val (initDay, initMonth, initYear) = if (parts.size == 3) {
        if (parts[0].length == 4) Triple(parts[2], parts[1], parts[0])
        else Triple(parts[0], parts[1], parts[2])
    } else Triple("01", "01", "2000")

    var selectedYearIndex  by remember { mutableIntStateOf(YEAR_LIST.indexOf(initYear).coerceAtLeast(0)) }
    var selectedMonthIndex by remember { mutableIntStateOf((initMonth.toIntOrNull() ?: 1) - 1) }
    var selectedDayIndex   by remember { mutableIntStateOf((initDay.toIntOrNull() ?: 1) - 1) }

    val daysInMonth by remember {
        derivedStateOf {
            val year = YEAR_LIST.getOrElse(selectedYearIndex) { "2000" }.toIntOrNull() ?: 2000
            val month = selectedMonthIndex + 1
            YearMonth.of(year, month).lengthOfMonth()
        }
    }
    LaunchedEffect(daysInMonth) {
        if (selectedDayIndex >= daysInMonth) selectedDayIndex = daysInMonth - 1
    }

    val dayList = remember(daysInMonth) { (1..daysInMonth).map { it.toString() } }

    val formattedDay   = (selectedDayIndex + 1).toString().padStart(2, '0')
    val formattedMonth = (selectedMonthIndex + 1).toString().padStart(2, '0')
    val formattedYear  = YEAR_LIST.getOrElse(selectedYearIndex) { "2000" }
    val formattedNewDate = "$formattedDay/$formattedMonth/$formattedYear"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BottomSheetHeader(title = stringResource(R.string.birthday), onClose = onClose)

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Lines
            Column(modifier = Modifier.fillMaxWidth().height(44.dp), verticalArrangement = Arrangement.SpaceBetween) {
                HorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.15f), thickness = 1.dp)
                HorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.15f), thickness = 1.dp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month – circular
                BirthdayWheelCircular(
                    items = listOf(
                        stringResource(R.string.month_jan_short),
                        stringResource(R.string.month_feb_short),
                        stringResource(R.string.month_mar_short),
                        stringResource(R.string.month_apr_short),
                        stringResource(R.string.month_may_short),
                        stringResource(R.string.month_jun_short),
                        stringResource(R.string.month_jul_short),
                        stringResource(R.string.month_aug_short),
                        stringResource(R.string.month_sep_short),
                        stringResource(R.string.month_oct_short),
                        stringResource(R.string.month_nov_short),
                        stringResource(R.string.month_dec_short)
                    ),
                    initialIndex = selectedMonthIndex,
                    onIndexChange = { selectedMonthIndex = it },
                    modifier = Modifier.weight(1.2f)
                )
                // Day – circular
                BirthdayWheelCircular(
                    items = dayList,
                    initialIndex = selectedDayIndex.coerceAtMost(daysInMonth - 1),
                    onIndexChange = { selectedDayIndex = it },
                    modifier = Modifier.weight(1f)
                )
                // Year – circular
                BirthdayWheelCircular(
                    items = YEAR_LIST,
                    initialIndex = selectedYearIndex,
                    onIndexChange = { selectedYearIndex = it },
                    modifier = Modifier.weight(1.2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                onBirthdaySelected(formattedNewDate)
                onClose()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(stringResource(R.string.done), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun ChangePasswordBottomSheetContent(
    onConfirm: (String, String) -> Unit,
    onClose: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    
    val isEnabled = oldPassword.isNotBlank() && 
                    newPassword.isNotBlank() && 
                    newPassword == confirmPassword && 
                    newPassword.length >= 6

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 24.dp)
    ) {
        BottomSheetHeader(title = stringResource(R.string.change_password), onClose = onClose)

        Column(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            MoonTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = stringResource(R.string.old_password),
                placeholderText = stringResource(R.string.placeholder_password),
                iconVector = Icons.Outlined.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            MoonTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = stringResource(R.string.new_password),
                placeholderText = stringResource(R.string.placeholder_password),
                iconVector = Icons.Outlined.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            val isError = confirmPassword.isNotBlank() && confirmPassword != newPassword
            MoonTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = stringResource(R.string.confirm_new_password),
                placeholderText = stringResource(R.string.placeholder_confirm_password),
                iconVector = Icons.Outlined.Lock,
                isPassword = true,
                errorText = if (isError) UiText.StringResource(R.string.passwords_do_not_match) else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        if (isEnabled) {
                            onConfirm(oldPassword, newPassword)
                        }
                    }
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onConfirm(oldPassword, newPassword) },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(54.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(stringResource(R.string.confirm), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun BirthdayWheelCircular(
    items: List<String>,
    initialIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp
) {
    val colorScheme = MaterialTheme.colorScheme
    val count = items.size
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val startIndex = (INFINITE_MULTIPLIER / 2) * count + initialIndex
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapFling = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedRealIndex by remember { derivedStateOf { listState.firstVisibleItemIndex % count } }

    LaunchedEffect(selectedRealIndex) { 
        onIndexChange(selectedRealIndex) 
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(modifier = modifier.height(itemHeight * 3), contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFling,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight)
        ) {
            items(INFINITE_MULTIPLIER * count) { flatIndex ->
                val ri = flatIndex % count
                val isSel = flatIndex == listState.firstVisibleItemIndex
                Box(Modifier.height(itemHeight).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = items[ri],
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        fontSize = if (isSel) 17.sp else 15.sp,
                        color = if (isSel) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.35f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Keep WheelPicker for backwards compatibility (not used in birthday anymore)
@Composable
fun WheelPicker(
    items: List<String>,
    initialValue: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 40.dp
    val startIndex = items.indexOf(initialValue).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val centerIndex = listState.firstVisibleItemIndex
        if (centerIndex in items.indices) {
            onItemSelected(items[centerIndex])
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * 5)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        )
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight * 2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val isSelected = listState.firstVisibleItemIndex == index
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
