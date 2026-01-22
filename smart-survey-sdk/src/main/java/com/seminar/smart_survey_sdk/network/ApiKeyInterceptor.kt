package com.seminar.smart_survey_sdk.network

import okhttp3.Interceptor
import okhttp3.Response

internal class ApiKeyInterceptor(
    private val apiKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val requestWithApiKey = original.newBuilder()
            .addHeader("X-API-Key", apiKey)
            .build()

        return chain.proceed(requestWithApiKey)
    }
}
