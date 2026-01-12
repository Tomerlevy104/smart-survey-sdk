package com.seminar.smart_survey_sdk.dto

data class SurveyResponseDto(val surveyId: String,
                             val answers: List<AnswerDto>)
