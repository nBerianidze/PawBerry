package com.example.pawberry.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.pawberry.data.db.PetEntity
import com.example.pawberry.data.remote.CreatePetRequest
import com.example.pawberry.data.remote.PawBerryApi
import com.example.pawberry.data.remote.UpdatePetRequest
import com.example.pawberry.data.remote.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class PetRepository(
    private val api: PawBerryApi,
    private val context: Context,
) {
    suspend fun getAll(): List<PetEntity> = api.getPets().map { it.toEntity() }

    suspend fun insert(
        name: String,
        species: String,
        breed: String,
        dateOfBirth: String,
        favoriteToy: String,
        gender: String,
        imageUri: Uri?,
        existingImage: String,
    ): PetEntity {
        val profileImage = uploadIfNeeded(imageUri) ?: existingImage
        return api.createPet(
            CreatePetRequest(
                name = name,
                species = species,
                breed = breed,
                dateOfBirth = dateOfBirth.ifBlank { null },
                favoriteToy = favoriteToy,
                gender = gender,
                profileImage = profileImage,
            )
        ).toEntity()
    }

    suspend fun update(
        pet: PetEntity,
        imageUri: Uri?,
    ): PetEntity {
        val profileImage = uploadIfNeeded(imageUri) ?: pet.profileImage
        return api.updatePet(
            pet.petId,
            UpdatePetRequest(
                name = pet.name,
                species = pet.species,
                breed = pet.breed,
                dateOfBirth = pet.dateOfBirth.ifBlank { null },
                favoriteToy = pet.favoriteToy,
                gender = pet.gender,
                profileImage = profileImage,
            ),
        ).toEntity()
    }

    suspend fun delete(petId: String) {
        api.deletePet(petId)
    }

    private suspend fun uploadIfNeeded(imageUri: Uri?): String? {
        if (imageUri == null) return null
        val bytes = withContext(Dispatchers.IO) { compressJpeg(imageUri) }
        val body = bytes.toRequestBody("image/jpeg".toMediaType())
        val part = MultipartBody.Part.createFormData("image", "pet.jpg", body)
        return api.uploadImage(part).url
    }

    private fun compressJpeg(uri: Uri): ByteArray {
        val resolver = context.contentResolver
        val original = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: throw IllegalArgumentException("Could not read the selected image.")
        val scaled = scaleBitmap(original, MAX_EDGE)
        val output = ByteArrayOutputStream()
        var quality = 90
        do {
            output.reset()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
            quality -= 10
        } while (output.size() > MAX_BYTES && quality >= 50)
        if (output.size() > MAX_BYTES) {
            throw IllegalArgumentException("Image must be 5 MB or smaller.")
        }
        return output.toByteArray()
    }

    private fun scaleBitmap(source: Bitmap, maxEdge: Int): Bitmap {
        val longest = maxOf(source.width, source.height)
        if (longest <= maxEdge) return source
        val scale = maxEdge.toFloat() / longest
        return Bitmap.createScaledBitmap(
            source,
            (source.width * scale).toInt().coerceAtLeast(1),
            (source.height * scale).toInt().coerceAtLeast(1),
            true,
        )
    }

    companion object {
        private const val MAX_EDGE = 1080
        private const val MAX_BYTES = 5 * 1024 * 1024
    }
}
