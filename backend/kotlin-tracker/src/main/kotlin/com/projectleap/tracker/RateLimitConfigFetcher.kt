package com.projectleap.tracker

import okhttp3.OkHttpClient
import okhttp3.Request
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class RateLimitConfigFetcher(
    private val client: OkHttpClient = OkHttpClient()
) {
    data class RateLimitConfig(val service: String, val limitPerSecond: Long, val burst: Long)

    fun fetch(collectorBaseUrl: String, token: String, service: String): RateLimitConfig? {
        val req = Request.Builder()
            .url("$collectorBaseUrl/api/rate-limit")
            .addHeader("Authorization", "Bearer $token")
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val mapper = jacksonObjectMapper()
            val configs: List<RateLimitConfig> = mapper.readValue(body)
            return configs.firstOrNull { it.service == service }
        }
    }
}
