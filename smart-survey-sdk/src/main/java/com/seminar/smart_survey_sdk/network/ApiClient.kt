package com.seminar.smart_survey_sdk.network

import com.seminar.smart_survey_sdk.SmartSurvey
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal object ApiClient {

    @Volatile
    private var surveyApi: SurveyApi? = null

    @Volatile
    private var surveyResponseApi: SurveyResponseApi? = null


    fun surveyApi(): SurveyApi {
        val existing = surveyApi
        if (existing != null) return existing

        synchronized(this) {
            val again = surveyApi
            if (again != null) return again

            val config = SmartSurvey.requireConfig()

            val okHttp = OkHttpClient.Builder()
                .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(config.baseUrl)
                .client(okHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val created = retrofit.create(SurveyApi::class.java)
            surveyApi = created
            return created
        }
    }

    fun surveyResponseApi(): SurveyResponseApi {
        val existing = surveyResponseApi
        if (existing != null) return existing

        synchronized(this) {
            val again = surveyResponseApi
            if (again != null) return again

            val config = SmartSurvey.requireConfig()

            val okHttp = OkHttpClient.Builder()
                .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(config.baseUrl)
                .client(okHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val created = retrofit.create(SurveyResponseApi::class.java)
            surveyResponseApi = created
            return created
        }
    }
}
