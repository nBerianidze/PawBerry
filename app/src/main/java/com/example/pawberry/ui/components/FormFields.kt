package com.example.pawberry.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.pawberry.R
import com.example.pawberry.util.formatDateForDisplay
import com.example.pawberry.util.formatTimeForDisplay
import com.example.pawberry.util.hourOf
import com.example.pawberry.util.isoTime
import com.example.pawberry.util.minuteOf
import java.util.Calendar
import java.util.Locale

/** Label above a full-width button that opens a menu or picker. */
@Composable
private fun SelectorField(
    label: String,
    value: String,
    trailingIcon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                )
                Icon(
                    painter = painterResource(trailingIcon),
                    contentDescription = null,
                )
            }
            content()
        }
    }
}

/** Generic dropdown backed by [DropdownMenu], used for pets, categories and repeats. */
@Composable
fun <T> DropdownField(
    label: String,
    options: List<T>,
    selectedOption: T?,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select",
) {
    var expanded by remember { mutableStateOf(false) }

    SelectorField(
        label = label,
        value = selectedOption?.let(optionLabel) ?: placeholder,
        trailingIcon = R.drawable.ic_arrow_drop_down,
        onClick = { expanded = true },
        modifier = modifier,
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Opens a date picker with independently scrollable month, day, and year columns. */
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    SelectorField(
        label = label,
        value = if (value.isBlank()) "Select a date" else formatDateForDisplay(value),
        trailingIcon = R.drawable.ic_reminders,
        onClick = { showDialog = true },
        modifier = modifier,
    )

    if (showDialog) {
        WheelDatePickerDialog(
            initialValue = value,
            onDismiss = { showDialog = false },
            onConfirm = { isoDate ->
                onDateSelected(isoDate)
                showDialog = false
            },
        )
    }
}

@Composable
private fun WheelDatePickerDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val today = remember { Calendar.getInstance() }
    val initial = remember(initialValue) { calendarFromIsoDate(initialValue) ?: today }
    var year by remember { mutableIntStateOf(initial.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(initial.get(Calendar.MONTH)) }
    var day by remember { mutableIntStateOf(initial.get(Calendar.DAY_OF_MONTH)) }

    val years = remember {
        val currentYear = today.get(Calendar.YEAR)
        (currentYear - 100..currentYear + 10).toList()
    }
    val months = remember { MONTH_LABELS }
    val daysInSelectedMonth = remember(year, month) { daysInMonth(year, month) }
    val days = remember(daysInSelectedMonth) { (1..daysInSelectedMonth).toList() }

    LaunchedEffect(daysInSelectedMonth) {
        if (day > daysInSelectedMonth) day = daysInSelectedMonth
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)) {
                Text(
                    text = "Select a date",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ScrollPickColumn(
                        items = months.indices.toList(),
                        selected = month,
                        label = { months[it] },
                        onSelected = { month = it },
                        modifier = Modifier.weight(1.1f),
                    )
                    ScrollPickColumn(
                        items = days,
                        selected = day.coerceAtMost(daysInSelectedMonth),
                        label = { it.toString() },
                        onSelected = { day = it },
                        modifier = Modifier.weight(0.8f),
                    )
                    ScrollPickColumn(
                        items = years,
                        selected = year,
                        label = { it.toString() },
                        onSelected = { year = it },
                        modifier = Modifier.weight(1.1f),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(
                        onClick = {
                            onConfirm(
                                String.format(
                                    Locale.US,
                                    "%04d-%02d-%02d",
                                    year,
                                    month + 1,
                                    day.coerceAtMost(daysInMonth(year, month)),
                                ),
                            )
                        },
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun <T : Any> ScrollPickColumn(
    items: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(items) {
        val index = items.indexOf(selected)
        if (index >= 0) {
            listState.scrollToItem((index - 2).coerceAtLeast(0))
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(items, key = { it }) { item ->
            val isSelected = item == selected
            Text(
                text = label(item),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                    )
                    .clickable { onSelected(item) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

private val MONTH_LABELS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

private fun daysInMonth(year: Int, monthZeroBased: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, monthZeroBased, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun calendarFromIsoDate(isoDate: String): Calendar? {
    val parts = isoDate.split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return Calendar.getInstance().apply {
        set(year, month - 1, day)
    }
}

/** Opens a Material 3 time picker and reports the choice back as `HH:mm`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    SelectorField(
        label = label,
        value = if (value.isBlank()) "Select a time" else formatTimeForDisplay(value),
        trailingIcon = R.drawable.ic_arrow_drop_down,
        onClick = { showDialog = true },
        modifier = modifier,
    )

    if (showDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = hourOf(value),
            initialMinute = minuteOf(value),
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(isoTime(timePickerState.hour, timePickerState.minute))
                        showDialog = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
            title = { Text(label) },
            text = { TimePicker(state = timePickerState) },
        )
    }
}

/** Validation message shown under a form. Renders nothing when there is no error. */
@Composable
fun FormErrorText(message: String?, modifier: Modifier = Modifier) {
    if (message == null) return
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.padding(top = 12.dp),
    )
}
