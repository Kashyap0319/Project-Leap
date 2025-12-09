package com.projectleap.tracker

import java.util.concurrent.atomic.AtomicLong

class RateLimiter(
    private val permitsPerSecond: Long = 100,
    private val maxBurst: Long = permitsPerSecond
) {
    private val available = AtomicLong(maxBurst)
    @Volatile private var lastRefillNanos: Long = System.nanoTime()

    /** Returns true if within limit; false if exceeded. Request still proceeds. */
    fun tryConsume(): Boolean {
        refill()
        while (true) {
            val current = available.get()
            if (current <= 0) return false
            if (available.compareAndSet(current, current - 1)) return true
        }
    }

    private fun refill() {
        synchronized(this) {
            val now = System.nanoTime()
            val elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000.0
            if (elapsedSeconds <= 0) return
            val toAdd = (elapsedSeconds * permitsPerSecond).toLong()
            if (toAdd <= 0) return
            val newLast = lastRefillNanos + (toAdd * 1_000_000_000L) / permitsPerSecond
            lastRefillNanos = newLast
            available.updateAndGet { cur -> (cur + toAdd).coerceAtMost(maxBurst) }
        }
    }
}
