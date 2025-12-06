package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class UserDocument(
    @Id val id: String? = null,
    @Indexed(unique = true) val username: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)
