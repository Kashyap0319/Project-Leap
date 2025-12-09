package com.projectleap.tracker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class RateLimiterTest {

    @Test
    fun `allows up to configured rate and then flags exceeded`() {
        val limiter = RateLimiter(permitsPerSecond = 5, maxBurst = 5)
        repeat(5) { assertThat(limiter.tryConsume()).isTrue() }
        assertThat(limiter.tryConsume()).isFalse()
    }

    @Test
    fun `refills over time`() {
        val limiter = RateLimiter(permitsPerSecond = 2, maxBurst = 2)
        repeat(2) { limiter.tryConsume() }
        Thread.sleep(600)
        assertThat(limiter.tryConsume()).isTrue()
    }

    @Test
    fun `thread safe under contention`() {
        val limiter = RateLimiter(permitsPerSecond = 50, maxBurst = 50)
        var allowed = 0
        val threads = (1..20).map {
            thread {
                repeat(10) {
                    if (limiter.tryConsume()) allowed++
                }
            }
        }
        threads.forEach { it.join() }
        assertThat(allowed).isLessThanOrEqualTo(50)
    }
}
