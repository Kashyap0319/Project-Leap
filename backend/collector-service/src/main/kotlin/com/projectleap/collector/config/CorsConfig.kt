package com.projectleap.collector.config

import com.projectleap.collector.ratelimit.RateLimitInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    private val rateLimitInterceptor: RateLimitInterceptor,
    @Value("\${CORS_ALLOWED_ORIGINS:\${cors.allowed-origins:http://localhost:3000,https://leapproject-a0trwsxd8-kashyap0319s-projects.vercel.app}}") private val allowedOrigins: String
) {
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addInterceptors(registry: InterceptorRegistry) {
                registry.addInterceptor(rateLimitInterceptor)
                    .addPathPatterns("/api/**")
                    .excludePathPatterns("/auth/**", "/api/v1/auth/**")
            }
            override fun addCorsMappings(registry: CorsRegistry) {
                val origins = allowedOrigins.split(",").map { it.trim() }.toTypedArray()
                registry.addMapping("/**")
                    .allowedOrigins(*origins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
            }
        }
    }
}
