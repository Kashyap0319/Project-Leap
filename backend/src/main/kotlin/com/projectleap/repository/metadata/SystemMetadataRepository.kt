package com.projectleap.repository.metadata

import com.projectleap.model.SystemMetadata
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SystemMetadataRepository : MongoRepository<SystemMetadata, String> {
    fun findByKey(key: String): Optional<SystemMetadata>
    fun findByCategory(category: String): List<SystemMetadata>
}
