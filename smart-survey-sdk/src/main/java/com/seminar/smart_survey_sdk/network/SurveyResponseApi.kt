package com.seminar.smart_survey_sdk.network

import com.seminar.smart_survey_sdk.dto.SurveyResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

internal interface SurveyResponseApi {

    // POST survey response -  api/sdk/survey-responses
    @POST("/api/sdk/survey-responses")
    suspend fun submitSurveyResponse(@Body body: SurveyResponseDto): SurveyResponseDto
}
