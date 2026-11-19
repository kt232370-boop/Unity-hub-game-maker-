package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unity_projects")
data class UnityProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val localPath: String,
    val template: String,
    val editorVersion: String,
    val targetPlatform: String,
    val isFavorite: Boolean = false,
    val lastModifiedEpochMs: Long = System.currentTimeMillis(),
    val description: String = "",
    val sceneCount: Int = 1
)
