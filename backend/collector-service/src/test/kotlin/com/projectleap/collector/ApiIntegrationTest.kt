package com.projectleap.collector

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectleap.collector.dto.LogRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@AutoConfigureWebMvc
class ApiIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc
    private var authToken: String = ""

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        // Signup and get token
        authToken = signupAndGetToken()
    }

    private fun signupAndGetToken(): String {
        val signupRequest = """
            {
                "username": "testuser${System.currentTimeMillis()}",
                "email": "test${System.currentTimeMillis()}@example.com",
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
    fun `test signup endpoint`() {
        val signupRequest = """
            {
                "username": "newuser${System.currentTimeMillis()}",
                "email": "new${System.currentTimeMillis()}@example.com",
                "password": "password123"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `test login endpoint`() {
        // First signup
        val username = "loginuser${System.currentTimeMillis()}"
        val signupRequest = """
            {
                "username": "$username",
                "email": "login${System.currentTimeMillis()}@example.com",
                "password": "password123"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupRequest)
        )

        // Then login
        val loginRequest = """
            {
                "username": "$username",
                "password": "password123"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `test protected endpoint without token returns 401`() {
        mockMvc.perform(get("/api/logs"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `test create log endpoint`() {
        val logRequest = LogRequest(
            service = "test-service",
            endpoint = "/api/test",
            method = "GET",
            statusCode = 200,
            latencyMs = 150,
            requestSize = 1024,
            responseSize = 2048
        )

        mockMvc.perform(
            post("/api/logs")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.service").value("test-service"))
    }

    @Test
    fun `test create log with high latency creates alert`() {
        val logRequest = LogRequest(
            service = "test-service",
            endpoint = "/api/slow",
            method = "GET",
            statusCode = 200,
            latencyMs = 600, // > 500ms
            requestSize = 1024,
            responseSize = 2048
        )

        mockMvc.perform(
            post("/api/logs")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logRequest))
        )
            .andExpect(status().isOk)

        // Verify alert was created
        mockMvc.perform(
            get("/api/alerts")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.type == 'LATENCY')]").exists())
    }

    @Test
    fun `test create log with 5xx status creates alert`() {
        val logRequest = LogRequest(
            service = "test-service",
            endpoint = "/api/error",
            method = "GET",
            statusCode = 500,
            latencyMs = 100,
            requestSize = 1024,
            responseSize = 512
        )

        mockMvc.perform(
            post("/api/logs")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logRequest))
        )
            .andExpect(status().isOk)

        // Verify alert was created
        mockMvc.perform(
            get("/api/alerts")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.type == 'ERROR')]").exists())
    }

    @Test
    fun `test get logs endpoint`() {
        mockMvc.perform(
            get("/api/logs?page=0&size=10")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `test get logs filtered by service`() {
        mockMvc.perform(
            get("/api/logs?service=test-service&page=0&size=10")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `test get alerts endpoint`() {
        mockMvc.perform(
            get("/api/alerts")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    fun `test get incidents endpoint`() {
        mockMvc.perform(
            get("/api/incidents")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    fun `test get services endpoint`() {
        mockMvc.perform(
            get("/api/services")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }
}

