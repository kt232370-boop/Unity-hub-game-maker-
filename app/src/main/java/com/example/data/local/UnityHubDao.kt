package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UnityHubDao {
    @Query("SELECT * FROM unity_projects ORDER BY isFavorite DESC, lastModifiedEpochMs DESC")
    fun observeProjects(): Flow<List<UnityProjectEntity>>

    @Query("SELECT COUNT(*) FROM unity_projects")
    suspend fun getProjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: UnityProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<UnityProjectEntity>)

    @Update
    suspend fun updateProject(project: UnityProjectEntity)

    @Query("DELETE FROM unity_projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Int)

    @Query("SELECT * FROM unity_tutorials ORDER BY isBookmarked DESC, id ASC")
    fun observeTutorials(): Flow<List<TutorialEntity>>

    @Query("SELECT COUNT(*) FROM unity_tutorials")
    suspend fun getTutorialCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutorials(tutorials: List<TutorialEntity>)

    @Update
    suspend fun updateTutorial(tutorial: TutorialEntity)
}
