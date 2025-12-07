package com.projectleap.collector.controller

import com.projectleap.collector.model.LogDocument
import com.projectleap.collector.service.LogIngestService
import com.projectleap.contracts.LogEvent
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/logs")
@Validated
class LogController(
    private val logIngestService: LogIngestService
) {

    @PostMapping("/batch")
    fun ingest(@RequestBody @Valid events: List<LogEvent>): ResponseEntity<Void> {
        logIngestService.saveBatch(events)
        return ResponseEntity.accepted().build()
    }

    @GetMapping
    fun query(
        @RequestParam(required = false) service: String?,
        @RequestParam(required = false) endpoint: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false, name = "slow") slow: Boolean?,
        @RequestParam(required = false, name = "broken") broken: Boolean?,
        @RequestParam(required = false, name = "rateLimited") rateLimited: Boolean?,
        @RequestParam(required = false, name = "errorsOnly") errorsOnly: Boolean?,
        @RequestParam(required = false, name = "startTs") startTs: Long?,
        @RequestParam(required = false, name = "endTs") endTs: Long?,
        @RequestParam(required = false, name = "q") q: String?,
        @RequestParam(required = false, name = "window") window: String?,
        @RequestParam(required = false, name = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, name = "size", defaultValue = "50") size: Int
    ): List<LogDocument> =
        logIngestService.query(service, endpoint, status, slow, broken, rateLimited, errorsOnly, startTs, endTs, q, window, page, size)
}
