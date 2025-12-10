package com.projectleap.collector

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureWebMvc
class ConcurrencyTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc
    private var authToken: String = ""

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        authToken = signupAndGetToken()
    }

    private fun signupAndGetToken(): String {
        val signupRequest = """
            {
                "username": "concurrency${System.currentTimeMillis()}",
                "email": "concurrency${System.currentTimeMillis()}@example.com",
                "password": "password123"
            }
        """.trimIndent()

        val result = mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupRequest)
        )
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, Map::class.java)
        return response["token"] as String
    }

    @Test
    fun `test concurrent incident resolution with version conflict`() {
        // First create an incident (if endpoint exists)
        // For now, we'll simulate by creating a test incident
        
        // Create incident via direct repository access would be ideal
        // But for API test, we'll test the resolve endpoint
        
        // This test requires:
        // 1. Create incident (get ID and version=1)
        // 2. Two threads try to resolve with version=1
        // 3. One should succeed, one should get 409 Conflict
        
        val executor = Executors.newFixedThreadPool(2)
        val latch = CountDownLatch(2)
        val successCount = AtomicInteger(0)
        val conflictCount = AtomicInteger(0)

        // Note: This test requires incident creation endpoint
        // For now, documenting the expected behavior
        
        // Thread 1
        executor.submit {
            try {
                val resolveRequest = """
                    {
                        "version": 1
                    }
                """.trimIndent()

                val result = mockMvc.perform(
                    post("/api/incidents/{id}/resolve", "test-incident-id")
                        .header("Authorization", "Bearer $authToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolveRequest)
                )
                    .andReturn()

                if (result.response.status == 200) {
                    successCount.incrementAndGet()
                } else if (result.response.status == 409) {
                    conflictCount.incrementAndGet()
                }
            } catch (e: Exception) {
                // Handle exception
            } finally {
                latch.countDown()
            }
        }

        // Thread 2 (immediately after)
        executor.submit {
            try {
                val resolveRequest = """
                    {
                        "version": 1
                    }
                """.trimIndent()

                val result = mockMvc.perform(
                    post("/api/incidents/{id}/resolve", "test-incident-id")
                        .header("Authorization", "Bearer $authToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolveRequest)
                )
                    .andReturn()

                if (result.response.status == 200) {
                    successCount.incrementAndGet()
                } else if (result.response.status == 409) {
                    conflictCount.incrementAndGet()
                }
            } catch (e: Exception) {
                // Handle exception
            } finally {
                latch.countDown()
            }
        }

        latch.await()
        executor.shutdown()

        // Verify: Exactly one success, one conflict
        assertTrue(successCount.get() == 1, "Expected exactly one successful resolve")
        assertTrue(conflictCount.get() == 1, "Expected exactly one version conflict")
    }

    @Test
    fun `test concurrent log ingestion`() {
        val executor = Executors.newFixedThreadPool(10)
        val latch = CountDownLatch(100)
        val successCount = AtomicInteger(0)

        repeat(100) { i ->
            executor.submit {
                try {
                    val logRequest = """
                        {
                            "service": "test-service",
                            "endpoint": "/api/test/$i",
                            "method": "GET",
                            "statusCode": 200,
                            "latencyMs": ${100 + i},
                            "requestSize": 1024,
                            "responseSize": 2048,
                            "rateLimited": false
                        }
                    """.trimIndent()

                    mockMvc.perform(
                        post("/api/logs")
                            .header("Authorization", "Bearer $authToken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(logRequest)
                    )
                        .andExpect(status().isOk)
                    
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // Log error
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // Verify all logs were saved
        assertTrue(successCount.get() == 100, "Expected all 100 logs to be saved")
    }
}

