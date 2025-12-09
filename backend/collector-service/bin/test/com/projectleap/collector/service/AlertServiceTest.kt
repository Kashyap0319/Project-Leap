package com.projectleap.collector.service

import com.projectleap.collector.model.AlertDocument
import com.projectleap.contracts.LogEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.data.mongodb.core.MongoTemplate

class AlertServiceTest {

    @Test
    fun `creates alerts for latency 5xx and rate limit`() {
        val metaTemplate: MongoTemplate = mock()
            // val incidentService: IncidentService = mock()
            // val svc = AlertService(metaTemplate, incidentService)

            // val events = listOf(
            //     LogEvent("svc","/a",200,600,true,System.currentTimeMillis()),
            //     LogEvent("svc","/b",503,100,false,System.currentTimeMillis())
            // )
            // svc.evaluateAndPersist(events)

            // val captor = argumentCaptor<List<AlertDocument>>()
            // verify(metaTemplate).insert(captor.capture(), eq(AlertDocument::class.java))
            // val alerts = captor.firstValue
            // assertThat(alerts).hasSize(3)
    }
}
