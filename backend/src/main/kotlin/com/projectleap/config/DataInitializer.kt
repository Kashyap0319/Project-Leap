package com.projectleap.config

import com.projectleap.service.UserService
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer {
    
    @Bean
    fun initializeData(userService: UserService) = CommandLineRunner {
        try {
            // Create default admin user if it doesn't exist
            if (userService.getUserByUsername("admin") == null) {
                userService.createUser(
                    username = "admin",
                    password = "admin123",
                    email = "admin@projectleap.com",
                    roles = setOf("USER", "ADMIN")
                )
                println("✅ Default admin user created: username=admin, password=admin123")
            }
            
            // Create default test user if it doesn't exist
            if (userService.getUserByUsername("testuser") == null) {
                userService.createUser(
                    username = "testuser",
                    password = "test123",
                    email = "test@projectleap.com",
                    roles = setOf("USER")
                )
                println("✅ Default test user created: username=testuser, password=test123")
            }
        } catch (e: Exception) {
            println("⚠️ Could not create default users: ${e.message}")
        }
    }
}
