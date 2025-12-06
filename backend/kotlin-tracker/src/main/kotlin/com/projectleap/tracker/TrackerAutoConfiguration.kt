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
    fun trackingRateLimiter(props: MonitoringProperties) =
        RateLimiter(permitsPerSecond = props.rateLimit.limit, maxBurst = props.rateLimit.burst)

    @Bean(destroyMethod = "close")
    fun logBatcher(props: MonitoringProperties, rateLimiter: RateLimiter): LogBatcher =
        LogBatcher(
            collectorBaseUrl = props.collectorUrl.trimEnd('/'),
            jwtService = JwtService(props.jwtSecret),
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
