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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawberry.ui.components.DatePickerField
import com.example.pawberry.ui.components.FormErrorText
import com.example.pawberry.ui.components.PawBerryTopBar

/**
 * One form shared by "Add Pet" and "Edit Pet". Passing the existing values in as
 * `initial*` turns it into an edit form; leaving them blank makes it an add form.
 */
@Composable
fun PetFormScreen(
    title: String,
    submitLabel: String,
    errorMessage: String?,
    onSubmit: (name: String, breed: String, dateOfBirth: String, favoriteToy: String) -> Unit,
    onCancel: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialBreed: String = "",
    initialDateOfBirth: String = "",
    initialFavoriteToy: String = "",
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var breed by rememberSaveable(initialBreed) { mutableStateOf(initialBreed) }
    var dateOfBirth by rememberSaveable(initialDateOfBirth) { mutableStateOf(initialDateOfBirth) }
    var favoriteToy by rememberSaveable(initialFavoriteToy) { mutableStateOf(initialFavoriteToy) }

    Column(modifier = modifier.fillMaxSize()) {
        PawBerryTopBar(title = title, onBack = onCancel)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    onClearError()
                },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = breed,
                onValueChange = {
                    breed = it
                    onClearError()
                },
                label = { Text("Breed") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            DatePickerField(
                label = "Date of Birth",
                value = dateOfBirth,
                onDateSelected = {
                    dateOfBirth = it
                    onClearError()
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = favoriteToy,
                onValueChange = {
                    favoriteToy = it
                    onClearError()
                },
                label = { Text("Favorite Toy") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            FormErrorText(message = errorMessage, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSubmit(name, breed, dateOfBirth, favoriteToy) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(submitLabel)
            }
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}
