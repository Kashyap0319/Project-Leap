package com.projectleap.collector.controller

import com.projectleap.collector.model.AlertDocument
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/alerts")
class AlertController(
    @Qualifier("metaTemplate") private val metaTemplate: MongoTemplate
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) service: String?,
        @RequestParam(required = false) endpoint: String?,
        @RequestParam(required = false, name = "type") type: String?,
        @RequestParam(required = false, name = "limit") limit: Int?
    ): List<AlertDocument> {
        val query = Query()
        service?.let { query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("service").`is`(it)) }
        endpoint?.let { query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("endpoint").`is`(it)) }
        type?.let { query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("type").`is`(it)) }
        query.with(Sort.by(Sort.Direction.DESC, "triggeredAt"))
        query.limit(limit ?: 200)
        return metaTemplate.find(query, AlertDocument::class.java)
    }
}
