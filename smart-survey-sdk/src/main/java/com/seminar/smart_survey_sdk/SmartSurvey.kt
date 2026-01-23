package com.seminar.smart_survey_sdk

import android.content.Context
import java.util.concurrent.TimeUnit

/**
 * SmartSurvey - Entry point for initializing and configuring the SDK.
 *
 * Usage (in the host app):
 * SmartSurvey.init(
 *   context = applicationContext,
 *   baseUrl = "https://survey-sdk-server.onrender.com/",
 *   apiKey = "YOUR_API_KEY"
 * )
 */
object SmartSurvey {

    // Immutable configuration used by the SDK.
    data class Config(
        val baseUrl: String,
        val apiKey: String,
        val connectTimeoutMs: Long = TimeUnit.SECONDS.toMillis(10),
        val readTimeoutMs: Long = TimeUnit.SECONDS.toMillis(20),
        val writeTimeoutMs: Long = TimeUnit.SECONDS.toMillis(20)
    )

    @Volatile
    private var config: Config? = null

    @Volatile
    private var appContext: Context? = null

    /**
     * Initialize the SDK once (typically from Application.onCreate()).
     * @param context Use applicationContext
     * @param baseUrl Base URL of your Smart Survey API server
     * @param apiKey  API key
     */
    @Synchronized
    fun init(
        context: Context,
        baseUrl: String,
        apiKey: String,
        connectTimeoutMs: Long = TimeUnit.SECONDS.toMillis(10),
        readTimeoutMs: Long = TimeUnit.SECONDS.toMillis(8),
        writeTimeoutMs: Long = TimeUnit.SECONDS.toMillis(20)
    ) {
        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)

        appContext = context.applicationContext
        config = Config(
            baseUrl = normalizedBaseUrl,
            apiKey = apiKey,
            connectTimeoutMs = connectTimeoutMs,
            readTimeoutMs = readTimeoutMs,
            writeTimeoutMs = writeTimeoutMs
        )
    }

    // Internal - returns the current SDK config, or throws if init() wasn't called.
    internal fun requireConfig(): Config {
        return config ?: throw IllegalStateException(
            "SmartSurvey SDK is not initialized. Call SmartSurvey.init(...) first."
        )
    }

    // Normalize the BaseUrl
    private fun normalizeBaseUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim()
        require(trimmed.isNotEmpty()) { "baseUrl must not be empty" }
        // Retrofit requires baseUrl to end with '/'
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
