package com.seminar.smart_survey_sdk.network

import okhttp3.Interceptor
import okhttp3.Response

// Interceptor to add the API key to each request
// Allows the backend to authenticate and identify the developer/application that is using the SDK.
// Prevents the host app from manually adding the API key in every Retrofit call.
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
