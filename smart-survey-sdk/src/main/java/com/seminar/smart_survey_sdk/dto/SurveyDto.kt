package com.seminar.smart_survey_sdk.dto

data class SurveyDto(
    val id: String,
    val title: String,
    val questions: List<QuestionDto>
)
