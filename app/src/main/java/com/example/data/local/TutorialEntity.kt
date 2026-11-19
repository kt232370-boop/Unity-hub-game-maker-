package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unity_tutorials")
data class TutorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val difficulty: String,
    val durationMinutes: Int,
    val summary: String,
    val stepsDelimited: String,
    val completedStepsBitmask: Int = 0,
    val isBookmarked: Boolean = false,
    val websiteUrl: String
) {
    val steps: List<String>
        get() = stepsDelimited.split("\n").filter { it.isNotBlank() }

    val completedCount: Int
        get() = steps.indices.count { idx -> (completedStepsBitmask and (1 shl idx)) != 0 }

    val progressFraction: Float
        get() = if (steps.isEmpty()) 0f else completedCount.toFloat() / steps.size.toFloat()

    fun isStepCompleted(index: Int): Boolean =
        (completedStepsBitmask and (1 shl index)) != 0
}
