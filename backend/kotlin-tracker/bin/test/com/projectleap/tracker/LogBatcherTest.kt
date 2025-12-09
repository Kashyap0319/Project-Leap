package com.projectleap.tracker

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.projectleap.contracts.LogEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class LogBatcherTest {

    private lateinit var server: MockWebServer
    private val mapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `sends batched logs to collector`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200))
        val jwt = TrackerJwtService("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")
        val batcher = LogBatcher(
            collectorBaseUrl = server.url("/").toString().trimEnd('/'),
            jwtService = jwt,
            issuerSubject = "tracker-test",
            client = OkHttpClient(),
            batchSize = 2,
            flushInterval = 200.milliseconds
        )

        val event = LogEvent(
            service = "svc",
            endpoint = "/ping",
            status = 200,
            latencyMs = 10,
            rateLimited = false,
            timestamp = System.currentTimeMillis()
        )
        batcher.enqueue(event)
        batcher.enqueue(event.copy(status = 201))

        delay(500)
        batcher.close()

        val recorded = server.takeRequest()
        val body = recorded.body.readUtf8()
        val sent = mapper.readTree(body)
        assertThat(sent.isArray).isTrue
        assertThat(sent.size()).isEqualTo(2)
    }
}
