package com.seminar.smart_survey_sdk.dto

data class QuestionDto(
    val id: String,
    val type: QuestionType,
    val text: String,

    // For RATING questions (optional for TEXT)
    val maxRating: Int? = null
)
