package com.seminar.smart_survey_sdk.dto

data class QuestionDto(
    val id: String,
    val type: QuestionType,
    val prompt: String,
    val options: List<String>? = null // null or empty for TEXT
 )
