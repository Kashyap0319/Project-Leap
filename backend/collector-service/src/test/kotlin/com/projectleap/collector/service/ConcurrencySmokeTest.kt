package com.projectleap.collector.service

import com.mongodb.bulk.BulkWriteResult
import com.projectleap.contracts.LogEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class ConcurrencySmokeTest {
    @Test
    fun `should handle 50 concurrent log batches without failure`() {
        val template = mock<MongoTemplate>()
        val alertService = mock<AlertService>()

        val captured = Collections.synchronizedList(mutableListOf<com.projectleap.collector.model.LogDocument>())
        val bulkOps = mock<BulkOperations>()

        whenever(template.bulkOps(BulkOperations.BulkMode.UNORDERED, com.projectleap.collector.model.LogDocument::class.java))
            .thenReturn(bulkOps)
        whenever(bulkOps.insert(any<com.projectleap.collector.model.LogDocument>())).thenAnswer {
            captured += it.getArgument(0)
            bulkOps
        }
        whenever(bulkOps.execute()).thenReturn(BulkWriteResult.unacknowledged())

        val service = LogIngestService(template, alertService)

        val pool = Executors.newFixedThreadPool(10)
        val latch = CountDownLatch(50)

        repeat(50) { idx ->
          pool.submit {
              try {
                  service.saveBatch(
                      listOf(
                          LogEvent(
                              service = "svc", endpoint = "/api/$idx", status = 200,
                              latencyMs = 42, rateLimited = false, timestamp = System.currentTimeMillis(), requestId = "$idx"
                          )
                      )
                  )
              } finally {
                  latch.countDown()
              }
          }
        }

        latch.await()
        pool.shutdownNow()

        assertEquals(50, captured.size)
    }
}
