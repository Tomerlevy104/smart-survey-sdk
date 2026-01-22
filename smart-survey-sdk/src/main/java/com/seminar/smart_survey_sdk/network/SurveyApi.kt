package com.seminar.smart_survey_sdk.network

import com.seminar.smart_survey_sdk.dto.SurveyDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface SurveyApi {

    // GET by ID -  http:/api/sdk/surveys/{id}
    @GET("/api/sdk/surveys/{id}")
    suspend fun getSurveyById(@Path("id") id: String): SurveyDto

    // GET random survey -  http:api/sdk/surveys/random"
    @GET("/api/sdk/surveys/random")
    suspend fun getRandomSurvey(): SurveyDto
}
