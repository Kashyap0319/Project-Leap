package com.projectleap

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "spring.data.mongodb.uri=mongodb://localhost:27017/test-logs",
    "mongodb.metadata.uri=mongodb://localhost:27017/test-metadata"
])
class ProjectLeapApplicationTests {

    @Test
    fun contextLoads() {
        // This test ensures the Spring context loads successfully
    }
}
