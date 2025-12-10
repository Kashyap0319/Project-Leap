package com.projectleap.collector.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        logger.debug("Validation error: ${ex.message}", ex)
        val errors = ex.bindingResult.fieldErrors.map {
            mapOf("field" to it.field, "message" to it.defaultMessage)
        }
        val body = mapOf(
            "status" to 400,
            "error" to "Bad Request",
            "errors" to errors
        )
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, String?>> {
        logger.warn("Illegal argument: ${ex.message}", ex)
        return ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<Map<String, String?>> {
        logger.error("Unhandled exception: ${ex.message}", ex)
        return ResponseEntity(mapOf("error" to (ex.message ?: "Internal server error")), HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
