package com.example.pawberry.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("userId")],
)
data class PetEntity(
    @PrimaryKey(autoGenerate = true) val petId: Long = 0,
    val userId: Long,
    val name: String,
    val breed: String,
    val dateOfBirth: String,
    val favoriteToy: String,
)
