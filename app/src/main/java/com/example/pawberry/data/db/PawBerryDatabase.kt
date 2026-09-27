package com.example.pawberry.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Bump [DATABASE_VERSION] every single time an entity changes (a new column, a renamed
 * field, a new table). Room stores a fingerprint of the schema inside the database file
 * and refuses to open a file whose fingerprint no longer matches the code, which is the
 * "Room cannot verify the data integrity" crash on launch.
 *
 * Because this project has no [androidx.room.migration.Migration]s yet, a version bump
 * rebuilds the database from scratch instead of crashing. That means existing accounts,
 * pets and reminders on the device are cleared — a normal app restart never clears
 * anything, only a schema change does.
 */
private const val DATABASE_VERSION = 1

@Database(
    entities = [UserEntity::class, PetEntity::class, ReminderEntity::class],
    version = DATABASE_VERSION,
    exportSchema = false,
)
abstract class PawBerryDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: PawBerryDatabase? = null

        fun getInstance(context: Context): PawBerryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PawBerryDatabase::class.java,
                    "pawberry.db",
                )
                    // No migrations are written yet, so when the version changes Room
                    // rebuilds the tables rather than refusing to open the file.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
