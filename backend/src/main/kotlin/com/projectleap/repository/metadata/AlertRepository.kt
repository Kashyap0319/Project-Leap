package com.projectleap.repository.metadata

import com.projectleap.model.Alert
import com.projectleap.model.AlertStatus
import com.projectleap.model.AlertType
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface AlertRepository : MongoRepository<Alert, String> {
    fun findByStatus(status: AlertStatus): List<Alert>
    fun findByType(type: AlertType): List<Alert>
    fun findByStatusAndType(status: AlertStatus, type: AlertType): List<Alert>
}
