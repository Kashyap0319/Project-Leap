package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class UserDocument(
    @Id val id: String? = null,
    val username: String,
    @Indexed(unique = true) val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)
