package com.projectleap.repository.metadata

import com.projectleap.model.RateLimitStat
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RateLimitStatRepository : MongoRepository<RateLimitStat, String> {
    fun findByUserId(userId: String): List<RateLimitStat>
    fun findByUserIdAndEndpoint(userId: String, endpoint: String): List<RateLimitStat>
}
