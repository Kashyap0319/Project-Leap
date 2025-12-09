package com.projectleap.collector.logs.repository

import com.projectleap.collector.model.LogEntry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant

interface LogEntryRepository : MongoRepository<LogEntry, String> {
    fun findByService(service: String, pageable: Pageable): Page<LogEntry>

    fun findByServiceAndTimestampBetween(
        service: String,
        from: Instant,
        to: Instant,
        pageable: Pageable
    ): Page<LogEntry>

    fun findByTimestampBetween(
        from: Instant,
        to: Instant,
        pageable: Pageable
    ): Page<LogEntry>
}
