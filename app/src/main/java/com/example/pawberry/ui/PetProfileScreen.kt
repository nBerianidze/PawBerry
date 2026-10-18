package com.example.pawberry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pawberry.R
import com.example.pawberry.data.db.PetEntity
import com.example.pawberry.data.model.PetGender
import com.example.pawberry.data.model.ReminderCategory
import com.example.pawberry.ui.components.ConfirmDialog
import com.example.pawberry.ui.components.PawBerryTopBar
import com.example.pawberry.util.formatDateForDisplay

@Composable
fun PetProfileScreen(
    pet: PetEntity?,
    imageBaseUrl: String,
    reminderCountByCategory: Map<ReminderCategory, Int>,
    onCategoryClick: (ReminderCategory) -> Unit,
    onEditPet: () -> Unit,
    onDeletePet: () -> Unit,
    onBack: () -> Unit,
    isBusy: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        PawBerryTopBar(title = pet?.name ?: "Pet", onBack = onBack)

        if (pet == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("This pet is no longer available.")
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val imageUrl = pet.imageUrl(imageBaseUrl)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                if (imageUrl.isBlank()) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pets),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp),
                    )
                } else {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "${pet.name} photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(label = "Species", value = pet.species.ifBlank { "Not set" })
                    DetailRow(
                        label = "Gender",
                        value = PetGender.fromApi(pet.gender)?.label ?: pet.gender.ifBlank { "Not set" },
                    )
                    DetailRow(label = "Breed", value = pet.breed)
                    DetailRow(
                        label = "Date of birth",
                        value = formatDateForDisplay(pet.dateOfBirth),
                    )
                    DetailRow(label = "Favorite toy", value = pet.favoriteToy)
                }
            }

            Text(
                text = "Care categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
            )

            ReminderCategory.petProfileCategories.forEach { category ->
                CategoryRow(
                    category = category,
                    reminderCount = reminderCountByCategory[category] ?: 0,
                    onClick = { onCategoryClick(category) },
                )
            }

            Button(
                onClick = onEditPet,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Edit Pet")
            }
            OutlinedButton(
                onClick = { confirmDelete = true },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Delete Pet")
            }
        }
    }

    if (confirmDelete && pet != null) {
        ConfirmDialog(
            title = "Delete ${pet.name}?",
            message = "This will permanently delete ${pet.name} and all of this pet's reminders.",
            onConfirm = {
                confirmDelete = false
                onDeletePet()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CategoryRow(
    category: ReminderCategory,
    reminderCount: Int,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.pluralLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (reminderCount == 0) {
                        "Nothing recorded yet"
                    } else {
                        "$reminderCount recorded"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
