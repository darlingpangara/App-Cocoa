package com.example.data

import com.example.model.CocoaSurvey
import kotlinx.coroutines.flow.Flow

class CocoaRepository(private val dao: CocoaSurveyDao) {
    val allSurveys: Flow<List<CocoaSurvey>> = dao.getAllSurveys()

    suspend fun getSurveyById(id: Long): CocoaSurvey? = dao.getSurveyById(id)

    suspend fun insert(survey: CocoaSurvey): Long = dao.insertSurvey(survey)

    suspend fun insertAll(surveys: List<CocoaSurvey>) = dao.insertAll(surveys)

    suspend fun update(survey: CocoaSurvey) = dao.updateSurvey(survey)

    suspend fun delete(survey: CocoaSurvey) = dao.deleteSurvey(survey)

    suspend fun deleteById(id: Long) = dao.deleteSurveyById(id)

    suspend fun deleteSampleSurveys() = dao.deleteSampleSurveys()

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun getCount(): Int = dao.getCount()
}
