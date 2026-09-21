package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.CocoaSurvey
import kotlinx.coroutines.flow.Flow

@Dao
interface CocoaSurveyDao {
    @Query("SELECT * FROM cocoa_surveys ORDER BY updatedAt DESC")
    fun getAllSurveys(): Flow<List<CocoaSurvey>>

    @Query("SELECT * FROM cocoa_surveys WHERE id = :id")
    suspend fun getSurveyById(id: Long): CocoaSurvey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: CocoaSurvey): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(surveys: List<CocoaSurvey>)

    @Update
    suspend fun updateSurvey(survey: CocoaSurvey)

    @Delete
    suspend fun deleteSurvey(survey: CocoaSurvey)

    @Query("DELETE FROM cocoa_surveys WHERE id = :id")
    suspend fun deleteSurveyById(id: Long)

    @Query("DELETE FROM cocoa_surveys WHERE farmerId IN ('KT-2026-001', 'TB-2026-042', 'PL-2026-018')")
    suspend fun deleteSampleSurveys()

    @Query("DELETE FROM cocoa_surveys")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM cocoa_surveys")
    suspend fun getCount(): Int
}
