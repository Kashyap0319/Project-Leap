package com.projectleap.tracker

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.projectleap.contracts.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.Closeable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class LogBatcher(
    private val collectorBaseUrl: String,
    private val jwtService: TrackerJwtService,
    private val issuerSubject: String,
    private val client: OkHttpClient = OkHttpClient(),
    private val batchSize: Int = 20,
    private val flushInterval: Duration = 2.seconds,
    private val maxQueue: Int = 1000
) : Closeable {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val channel = Channel<LogEvent>(capacity = maxQueue, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val mapper = jacksonObjectMapper().registerKotlinModule()
    private val mediaType = "application/json".toMediaType()
    @Volatile private var closed = false

    init {
        scope.launch { pump() }
    }

    fun enqueue(event: LogEvent) {
        if (closed) return
        channel.trySend(event)
    }

    private suspend fun pump() {
        val buffer = mutableListOf<LogEvent>()
        var lastFlush = System.nanoTime()
        while (!closed) {
            val timeoutMs = flushInterval.inWholeMilliseconds
            val next = withTimeoutOrNull(timeoutMs) { channel.receive() }
            if (next != null) buffer += next
            val timeElapsed = (System.nanoTime() - lastFlush) / 1_000_000
            if (buffer.size >= batchSize || timeElapsed >= timeoutMs || (next == null && buffer.isNotEmpty())) {
                sendBatch(buffer.toList())
                buffer.clear()
                lastFlush = System.nanoTime()
            }
        }
        val remaining = mutableListOf<LogEvent>()
        while (!channel.isEmpty) {
            channel.tryReceive().getOrNull()?.let { remaining += it }
        }
        if (remaining.isNotEmpty()) sendBatch(remaining)
    }

    private suspend fun sendBatch(batch: List<LogEvent>) {
        val token = jwtService.sign(issuerSubject, ttlSeconds = 300)
        val body = mapper.writeValueAsString(batch).toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$collectorBaseUrl/api/logs/batch")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        var attempt = 0
        var delayMs = 250L
        while (attempt < 3) {
            try {
                client.newCall(request).execute().use { resp ->
                    if (resp.isSuccessful) return
                }
            } catch (_: Exception) { }
            attempt++
            delay(delayMs)
            delayMs *= 2
        }
        batch.forEach { channel.trySend(it) }
    }

    override fun close() {
        closed = true
        channel.close()
    }
}
