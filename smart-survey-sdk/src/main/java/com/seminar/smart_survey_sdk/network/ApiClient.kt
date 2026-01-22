package com.seminar.smart_survey_sdk.network

import com.seminar.smart_survey_sdk.SmartSurvey
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal object ApiClient {

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var okHttp: OkHttpClient? = null

    @Volatile
    private var surveyApi: SurveyApi? = null

    @Volatile
    private var surveyResponseApi: SurveyResponseApi? = null

    private fun getOkHttp(): OkHttpClient {
        val existing = okHttp
        if (existing != null) return existing

        synchronized(this) {
            val again = okHttp
            if (again != null) return again

            val config = SmartSurvey.requireConfig()

            val created = OkHttpClient.Builder()
                .addInterceptor(ApiKeyInterceptor(config.apiKey))
                .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
                .build()

            okHttp = created
            return created
        }
    }

    private fun getRetrofit(): Retrofit {
        val existing = retrofit
        if (existing != null) return existing

        synchronized(this) {
            val again = retrofit
            if (again != null) return again

            val config = SmartSurvey.requireConfig()

            val created = Retrofit.Builder()
                .baseUrl(config.baseUrl)
                .client(getOkHttp())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit = created
            return created
        }
    }

    fun surveyApi(): SurveyApi {
        val existing = surveyApi
        if (existing != null) return existing

        synchronized(this) {
            val again = surveyApi
            if (again != null) return again

            val created = getRetrofit().create(SurveyApi::class.java)
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

            val created = getRetrofit().create(SurveyResponseApi::class.java)
            surveyResponseApi = created
            return created
        }
    }
}

