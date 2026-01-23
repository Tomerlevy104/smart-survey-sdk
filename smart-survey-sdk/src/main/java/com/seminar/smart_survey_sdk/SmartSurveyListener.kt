package com.seminar.smart_survey_sdk

/**
 * SmartSurveyListener - Interface for receiving survey events.
 */
interface SmartSurveyListener {

    // Called when the survey data was successfully loaded.
    fun onSurveyLoaded(surveyId: String)

    // Called when the survey was successfully submitted.
    fun onSurveySubmitted(surveyId: String)

    // Called on any error during loading or submitting a survey.
    fun onError(error: Throwable)
}
