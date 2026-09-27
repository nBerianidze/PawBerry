package com.example.pawberry.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pawberry.R
import com.example.pawberry.util.formatDateForDisplay
import com.example.pawberry.util.formatTimeForDisplay
import com.example.pawberry.util.hourOf
import com.example.pawberry.util.isoDateFromUtcMillis
import com.example.pawberry.util.isoTime
import com.example.pawberry.util.minuteOf
import com.example.pawberry.util.utcMillisFromIsoDate

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

/** Opens a Material 3 date picker and reports the choice back as `yyyy-MM-dd`. */
@OptIn(ExperimentalMaterial3Api::class)
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
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = utcMillisFromIsoDate(value),
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateSelected(isoDateFromUtcMillis(it))
                        }
                        showDialog = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
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
