package com.projectleap.collector.alerts.repository

import com.projectleap.collector.model.Alert
import org.springframework.data.mongodb.repository.MongoRepository

interface AlertRepository : MongoRepository<Alert, String> {
    fun findByResolved(resolved: Boolean): List<Alert>
}
