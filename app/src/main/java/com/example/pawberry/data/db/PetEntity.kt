package com.example.pawberry.data.db

data class PetEntity(
    val petId: String,
    val userId: String,
    val name: String,
    val breed: String,
    val dateOfBirth: String,
    val favoriteToy: String,
    val species: String = "",
    val gender: String = "",
    val profileImage: String = "",
) {
    fun imageUrl(baseUrl: String): String {
        if (profileImage.isBlank()) return ""
        if (profileImage.startsWith("http://") || profileImage.startsWith("https://")) {
            return profileImage
        }
        val root = baseUrl.trimEnd('/')
        return if (profileImage.startsWith("/")) "$root$profileImage" else "$root/$profileImage"
    }
}
