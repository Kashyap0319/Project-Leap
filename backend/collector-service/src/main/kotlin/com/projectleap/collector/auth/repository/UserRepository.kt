package com.projectleap.collector.auth.repository

import com.projectleap.collector.model.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.util.Optional

interface UserRepository : MongoRepository<User, String> {
    @Query("{ 'username': ?0 }")
    fun findByUsername(username: String): Optional<User>
    
    @Query("{ 'email': ?0 }")
    fun findByEmail(email: String): Optional<User>
}
