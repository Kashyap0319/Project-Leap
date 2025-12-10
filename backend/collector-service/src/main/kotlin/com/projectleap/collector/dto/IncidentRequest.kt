package com.projectleap.collector.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

// DTO for creating a new incident
// Adjust fields as needed for your model

data class IncidentRequest(
    @field:NotBlank val service: String,
    @field:NotBlank val endpoint: String,
    @field:NotBlank val severity: String,
    @field:NotBlank val message: String
)
