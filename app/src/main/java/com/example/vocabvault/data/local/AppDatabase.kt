package com.example.vocabvault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Single Room database for the app.
 * Increment [version] and provide a [androidx.room.migration.Migration] whenever
 * the schema changes to preserve user data.
 */
@Database(
    entities = [WordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}
