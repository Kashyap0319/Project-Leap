package com.projectleap.collector.controller

import com.projectleap.collector.model.RateLimitConfigDocument
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rate-limit")
class RateLimitController(
    @Qualifier("metaTemplate") private val metaTemplate: MongoTemplate
) {
    data class UpsertRequest(val service: String, val limitPerSecond: Long, val burst: Long? = null)

    @GetMapping
    fun list(): List<RateLimitConfigDocument> = metaTemplate.find(Query(), RateLimitConfigDocument::class.java)

    @PostMapping
    @Transactional("metaTxManager")
    fun upsert(@RequestBody req: UpsertRequest): ResponseEntity<RateLimitConfigDocument> {
        val query = Query(Criteria.where("service").`is`(req.service))
        val update = Update()
            .set("service", req.service)
            .set("limitPerSecond", req.limitPerSecond)
            .set("burst", req.burst ?: req.limitPerSecond)
            .set("updatedAt", System.currentTimeMillis())
        val result = metaTemplate.findAndModify(query, update, org.springframework.data.mongodb.core.FindAndModifyOptions.options().upsert(true).returnNew(true), RateLimitConfigDocument::class.java)!!
        return ResponseEntity.ok(result)
    }
}
