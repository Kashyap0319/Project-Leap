package com.projectleap.collector.logs.controller

import com.projectleap.collector.dto.LogRequest
import com.projectleap.collector.logs.service.LogService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/logs")
class LogsController(
    private val logService: LogService
) {

    @PostMapping
    fun saveLog(@Valid @RequestBody request: LogRequest) =
        ResponseEntity.ok(logService.saveLog(request))

    @GetMapping
    fun getLogs(
        @RequestParam(required = false) service: String?,
        @RequestParam(required = false) from: Instant?,
        @RequestParam(required = false) to: Instant?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ) = ResponseEntity.ok(
        logService.getLogs(service, from, to, PageRequest.of(page, size))
    )
}
