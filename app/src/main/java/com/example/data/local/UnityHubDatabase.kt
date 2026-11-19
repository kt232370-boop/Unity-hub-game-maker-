package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UnityProjectEntity::class, TutorialEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UnityHubDatabase : RoomDatabase() {
    abstract fun unityHubDao(): UnityHubDao

    companion object {
        @Volatile
        private var INSTANCE: UnityHubDatabase? = null

        fun getInstance(context: Context): UnityHubDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    UnityHubDatabase::class.java,
                    "unity_hub_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
