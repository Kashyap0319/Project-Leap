package com.projectleap.tracker

import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.filter.OncePerRequestFilter

@AutoConfiguration
@EnableConfigurationProperties(MonitoringProperties::class)
class TrackerAutoConfiguration {

    @Bean
    fun trackingJwtService(props: MonitoringProperties) = TrackerJwtService(props.jwtSecret)

    @Bean
    fun rateLimitConfigFetcher() = RateLimitConfigFetcher()

    @Bean
    fun trackingRateLimiter(props: MonitoringProperties, fetcher: RateLimitConfigFetcher, jwt: TrackerJwtService): RateLimiter {
        val baseUrl = props.collectorUrl.trimEnd('/')
        val token = jwt.sign(props.serviceName, ttlSeconds = 300)
        val override = runCatching { fetcher.fetch(baseUrl, token, props.serviceName) }.getOrNull()
        val limit = override?.limitPerSecond ?: props.rateLimit.limit
        val burst = override?.burst ?: props.rateLimit.burst
        return RateLimiter(permitsPerSecond = limit, maxBurst = burst)
    }

    @Bean(destroyMethod = "close")
    fun logBatcher(props: MonitoringProperties, rateLimiter: RateLimiter, jwt: TrackerJwtService): LogBatcher =
        LogBatcher(
            collectorBaseUrl = props.collectorUrl.trimEnd('/'),
            jwtService = jwt,
            issuerSubject = props.serviceName,
            client = OkHttpClient()
        )

    @Bean
    fun serverTrackingFilter(props: MonitoringProperties, rateLimiter: RateLimiter, batcher: LogBatcher): OncePerRequestFilter =
        ServerTrackingFilter(
            serviceName = props.serviceName,
            rateLimiter = rateLimiter,
            batcher = batcher
        )
}
