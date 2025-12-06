package com.projectleap.tracker

import com.projectleap.contracts.LogEvent
import okhttp3.Interceptor
import okhttp3.Response
import java.time.Instant

class TrackingInterceptor(
    private val serviceName: String,
    private val rateLimiter: RateLimiter = RateLimiter(),
    private val batcher: LogBatcher
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startMs = Instant.now().toEpochMilli()
        val allowed = rateLimiter.tryConsume()
        var status = 599
        var respBytes: Long? = null

        return try {
            val response = chain.proceed(request)
            status = response.code
            respBytes = response.body?.contentLength()?.takeIf { it >= 0 }
            response
        } finally {
            val endMs = Instant.now().toEpochMilli()
            val event = LogEvent(
                service = serviceName,
                endpoint = request.url.encodedPath,
                method = request.method,
                status = status,
                latencyMs = endMs - startMs,
                rateLimited = !allowed,
                timestamp = startMs,
                requestId = request.header("X-Request-Id"),
                requestBytes = request.body?.contentLength()?.takeIf { it >= 0 },
                responseBytes = respBytes
            )
            batcher.enqueue(event)
        }
    }
}
