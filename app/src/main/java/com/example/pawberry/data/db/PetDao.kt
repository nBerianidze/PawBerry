package com.example.pawberry.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: PetEntity): Long

    @Update
    suspend fun update(pet: PetEntity)

    @Query("SELECT * FROM pets WHERE userId = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeByUser(userId: Long): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE petId = :petId LIMIT 1")
    fun observeById(petId: Long): Flow<PetEntity?>
}
