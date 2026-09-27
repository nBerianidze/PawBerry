package com.example.pawberry.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.example.pawberry.data.db.PetEntity
import com.example.pawberry.data.model.ReminderCategory
import com.example.pawberry.data.model.RepeatFrequency
import com.example.pawberry.ui.components.DatePickerField
import com.example.pawberry.ui.components.DropdownField
import com.example.pawberry.ui.components.FormErrorText
import com.example.pawberry.ui.components.PawBerryTopBar
import com.example.pawberry.ui.components.TimePickerField

@Composable
fun AddReminderScreen(
    pets: List<PetEntity>,
    errorMessage: String?,
    onSave: (
        petId: Long?,
        title: String,
        category: ReminderCategory,
        dueDate: String,
        dueTime: String,
        repeatFrequency: RepeatFrequency,
    ) -> Unit,
    onCancel: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
    preselectedPetId: Long? = null,
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

            FormErrorText(message = errorMessage, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(selectedPetId, title, category, dueDate, dueTime, repeatFrequency)
                },
                enabled = pets.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Reminder")
            }
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}
