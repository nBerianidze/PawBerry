package com.example.pawberry.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pawberry.R
import com.example.pawberry.data.model.PetGender
import com.example.pawberry.data.model.PetSpecies
import com.example.pawberry.ui.components.DatePickerField
import com.example.pawberry.ui.components.DropdownField
import com.example.pawberry.ui.components.FormErrorText
import com.example.pawberry.ui.components.PawBerryTopBar
import com.example.pawberry.ui.components.SearchablePickerField

@Composable
fun PetFormScreen(
    title: String,
    submitLabel: String,
    errorMessage: String?,
    onSubmit: (
        name: String,
        species: String,
        breed: String,
        dateOfBirth: String,
        favoriteToy: String,
        gender: String,
        imageUri: Uri?,
    ) -> Unit,
    onCancel: () -> Unit,
    onClearError: () -> Unit,
    isBusy: Boolean = false,
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialSpecies: String = "",
    initialBreed: String = "",
    initialDateOfBirth: String = "",
    initialFavoriteToy: String = "",
    initialGender: String = "",
    initialImageUrl: String = "",
) {
    val predefinedSelected = PetSpecies.displayList().firstOrNull {
        it.equals(initialSpecies, ignoreCase = true)
    }
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var speciesChoice by rememberSaveable(initialSpecies) {
        mutableStateOf(
            when {
                initialSpecies.isBlank() -> ""
                predefinedSelected != null -> predefinedSelected
                else -> PetSpecies.OTHER
            }
        )
    }
    var customSpecies by rememberSaveable(initialSpecies) {
        mutableStateOf(if (predefinedSelected == null) initialSpecies else "")
    }
    var breed by rememberSaveable(initialBreed) { mutableStateOf(initialBreed) }
    var dateOfBirth by rememberSaveable(initialDateOfBirth) { mutableStateOf(initialDateOfBirth) }
    var favoriteToy by rememberSaveable(initialFavoriteToy) { mutableStateOf(initialFavoriteToy) }
    var gender by rememberSaveable(initialGender) {
        mutableStateOf(PetGender.fromApi(initialGender))
    }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            imageUri = uri.toString()
            onClearError()
        }
    }

    val preview = imageUri ?: initialImageUrl.takeIf { it.isNotBlank() }

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
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                if (preview.isNullOrBlank()) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pets),
                        contentDescription = "Pet photo placeholder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp),
                    )
                } else {
                    AsyncImage(
                        model = preview,
                        contentDescription = "Pet photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            ) {
                Text(if (preview.isNullOrBlank()) "Add photo" else "Replace photo")
            }
            Spacer(modifier = Modifier.height(16.dp))

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
            SearchablePickerField(
                label = "Species",
                options = PetSpecies.displayList(),
                selected = speciesChoice,
                onSelected = {
                    speciesChoice = it
                    if (it != PetSpecies.OTHER) customSpecies = ""
                    onClearError()
                },
                placeholder = "Select a species",
            )
            if (speciesChoice == PetSpecies.OTHER) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = customSpecies,
                    onValueChange = {
                        customSpecies = it
                        onClearError()
                    },
                    label = { Text("Custom species") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            DropdownField(
                label = "Gender",
                options = PetGender.entries,
                selectedOption = gender,
                optionLabel = { it.label },
                onOptionSelected = {
                    gender = it
                    onClearError()
                },
                placeholder = "Select gender",
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
                onClick = {
                    val species = if (speciesChoice == PetSpecies.OTHER) {
                        customSpecies.trim()
                    } else {
                        speciesChoice
                    }
                    onSubmit(
                        name,
                        species,
                        breed,
                        dateOfBirth,
                        favoriteToy,
                        gender?.label.orEmpty(),
                        imageUri?.let(Uri::parse),
                    )
                },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isBusy) "Saving…" else submitLabel)
            }
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}
