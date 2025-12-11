package com.projectleap.collector.config

import com.projectleap.collector.ratelimit.RateLimitInterceptor
import com.projectleap.collector.tracking.ApiTrackingInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    private val rateLimitInterceptor: RateLimitInterceptor,
    private val apiTrackingInterceptor: ApiTrackingInterceptor,
    @Value("\${CORS_ALLOWED_ORIGINS:\${cors.allowed-origins:http://localhost:3000,https://leapproject.vercel.app}}") private val allowedOrigins: String
) {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        val origins = allowedOrigins.split(",").map { it.trim() }
        configuration.allowedOrigins = origins
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addInterceptors(registry: InterceptorRegistry) {
                // API tracking interceptor - tracks all API calls
                registry.addInterceptor(apiTrackingInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/health", "/favicon.ico", "/actuator/**")
                
                // Rate limit interceptor
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
