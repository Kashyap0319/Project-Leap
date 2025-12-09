package com.projectleap.collector.service

import com.projectleap.collector.model.IncidentDocument
import com.projectleap.contracts.AlertType
import com.projectleap.contracts.IncidentStatus
import com.projectleap.contracts.Severity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class IncidentServiceTest {

    @Test
    fun `throws on version mismatch`() {
        val meta: MongoTemplate = mock()
        whenever(meta.findAndModify(any<Query>(), any<Update>(), any<FindAndModifyOptions>(), any<Class<IncidentDocument>>()))
            .thenReturn(null)
        val svc = IncidentService(meta)
        assertThatThrownBy { svc.resolve("id", 1) }
            .isInstanceOf(OptimisticLockingFailureException::class.java)
    }

    @Test
    fun `increments version on resolve`() {
        val meta: MongoTemplate = mock()
        val doc = IncidentDocument(
            id = "id",
            service = "svc",
            endpoint = "/e",
            type = AlertType.LATENCY,
            status = IncidentStatus.OPEN,
            firstSeen = 1,
            lastSeen = 1,
            occurrences = 1,
            severity = Severity.MEDIUM,
            version = 1
        )
        whenever(meta.findAndModify(any<Query>(), any<Update>(), any<FindAndModifyOptions>(), any<Class<IncidentDocument>>()))
            .thenReturn(doc.copy(status = IncidentStatus.RESOLVED, version = 2))
        val svc = IncidentService(meta)
        val resolved = svc.resolve("id", 1)
        assertThat(resolved.version).isEqualTo(2)
        assertThat(resolved.status).isEqualTo(IncidentStatus.RESOLVED)
    }
    // Disabled: broken test file
}
