package com.example.pawberry.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pawberry.data.db.PetEntity
import com.example.pawberry.data.model.RecurrenceRule
import com.example.pawberry.data.model.RecurrenceUnit
import com.example.pawberry.data.model.ReminderCategory
import com.example.pawberry.data.model.RepeatFrequency
import com.example.pawberry.ui.components.DatePickerField
import com.example.pawberry.ui.components.DropdownField
import com.example.pawberry.ui.components.FormErrorText
import com.example.pawberry.ui.components.PawBerryTopBar
import com.example.pawberry.ui.components.TimePickerField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddReminderScreen(
    pets: List<PetEntity>,
    errorMessage: String?,
    onSave: (
        petId: String?,
        title: String,
        category: ReminderCategory,
        dueDate: String,
        dueTime: String,
        recurrence: RecurrenceRule,
    ) -> Unit,
    onCancel: () -> Unit,
    onClearError: () -> Unit,
    isBusy: Boolean = false,
    modifier: Modifier = Modifier,
    preselectedPetId: String? = null,
    preselectedCategory: ReminderCategory? = null,
) {
    var selectedPetId by rememberSaveable {
        mutableStateOf(preselectedPetId ?: pets.firstOrNull()?.petId)
    }
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable {
        mutableStateOf(preselectedCategory ?: ReminderCategory.VACCINATION)
    }
    var dueDate by rememberSaveable { mutableStateOf("") }
    var dueTime by rememberSaveable { mutableStateOf("") }
    var repeatFrequency by rememberSaveable { mutableStateOf(RepeatFrequency.NONE) }
    var intervalText by rememberSaveable { mutableStateOf("1") }
    var unit by rememberSaveable { mutableStateOf(RecurrenceUnit.DAY) }
    var selectedWeekDays by rememberSaveable { mutableStateOf(intArrayOf()) }
    var monthDayText by rememberSaveable { mutableStateOf("") }
    var endsOnDate by rememberSaveable { mutableStateOf(false) }
    var endDate by rememberSaveable { mutableStateOf("") }

    val selectedPet = remember(pets, selectedPetId) {
        pets.firstOrNull { it.petId == selectedPetId }
    }

    Column(modifier = modifier.fillMaxSize()) {
        PawBerryTopBar(title = "Add Reminder", onBack = onCancel)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            if (pets.isEmpty()) {
                Text(
                    text = "Add a pet first — reminders always belong to one of your pets.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            DropdownField(
                label = "Pet",
                options = pets,
                selectedOption = selectedPet,
                optionLabel = { it.name },
                onOptionSelected = {
                    selectedPetId = it.petId
                    onClearError()
                },
                placeholder = "Select a pet",
            )
            Spacer(modifier = Modifier.height(16.dp))

            DropdownField(
                label = "Reminder Type",
                options = ReminderCategory.entries,
                selectedOption = category,
                optionLabel = { it.label },
                onOptionSelected = {
                    category = it
                    onClearError()
                },
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    onClearError()
                },
                label = { Text("Reminder Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))

            DatePickerField(
                label = "Date",
                value = dueDate,
                onDateSelected = {
                    dueDate = it
                    onClearError()
                },
            )
            Spacer(modifier = Modifier.height(16.dp))

            TimePickerField(
                label = "Time",
                value = dueTime,
                onTimeSelected = {
                    dueTime = it
                    onClearError()
                },
            )
            Spacer(modifier = Modifier.height(16.dp))

            DropdownField(
                label = "Repeat Frequency",
                options = RepeatFrequency.entries,
                selectedOption = repeatFrequency,
                optionLabel = { it.label },
                onOptionSelected = {
                    repeatFrequency = it
                    onClearError()
                },
            )

            if (repeatFrequency == RepeatFrequency.CUSTOM) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = {
                        intervalText = it.filter(Char::isDigit).ifBlank { "" }
                        onClearError()
                    },
                    label = { Text("Repeat every") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                DropdownField(
                    label = "Interval unit",
                    options = RecurrenceUnit.entries,
                    selectedOption = unit,
                    optionLabel = { it.label },
                    onOptionSelected = {
                        unit = it
                        onClearError()
                    },
                )
                if (unit == RecurrenceUnit.WEEK) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Days of the week", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        RecurrenceRule.WEEKDAY_LABELS.forEachIndexed { index, label ->
                            FilterChip(
                                selected = selectedWeekDays.contains(index),
                                onClick = {
                                    selectedWeekDays = if (selectedWeekDays.contains(index)) {
                                        selectedWeekDays.filterNot { it == index }.toIntArray()
                                    } else {
                                        (selectedWeekDays.toList() + index).sorted().toIntArray()
                                    }
                                    onClearError()
                                },
                                label = { Text(label) },
                            )
                        }
                    }
                }
                if (unit == RecurrenceUnit.MONTH) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = monthDayText,
                        onValueChange = {
                            monthDayText = it.filter(Char::isDigit).take(2)
                            onClearError()
                        },
                        label = { Text("Day of month (1-31)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (repeatFrequency != RepeatFrequency.NONE) {
                Spacer(modifier = Modifier.height(16.dp))
                DropdownField(
                    label = "Ends",
                    options = listOf(false, true),
                    selectedOption = endsOnDate,
                    optionLabel = { if (it) "On a date" else "Never" },
                    onOptionSelected = {
                        endsOnDate = it
                        onClearError()
                    },
                )
                if (endsOnDate) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DatePickerField(
                        label = "End date",
                        value = endDate,
                        onDateSelected = {
                            endDate = it
                            onClearError()
                        },
                    )
                }
            }

            FormErrorText(message = errorMessage, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val interval = intervalText.toIntOrNull() ?: 1
                    val recurrence = RecurrenceRule(
                        frequency = repeatFrequency,
                        interval = interval.coerceAtLeast(1),
                        unit = unit,
                        weekDays = selectedWeekDays.toList(),
                        monthDay = monthDayText.toIntOrNull()?.takeIf { it in 1..31 },
                        endsOnDate = endsOnDate && repeatFrequency != RepeatFrequency.NONE,
                        endDate = endDate,
                    )
                    onSave(selectedPetId, title, category, dueDate, dueTime, recurrence)
                },
                enabled = pets.isNotEmpty() && !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isBusy) "Saving…" else "Save Reminder")
            }
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}
