package com.projectleap.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(
    basePackages = ["com.projectleap.repository.logs"],
    mongoTemplateRef = "logsMongoTemplate"
)
class LogsMongoConfig {
    
    @Value("\${spring.data.mongodb.uri}")
    private lateinit var logsMongoUri: String
    
    @Primary
    @Bean(name = ["logsMongoClient"])
    fun logsMongoClient(): MongoClient {
        return MongoClients.create(logsMongoUri)
    }
    
    @Primary
    @Bean(name = ["logsMongoDatabaseFactory"])
    fun logsMongoDatabaseFactory(): MongoDatabaseFactory {
        return SimpleMongoClientDatabaseFactory(logsMongoClient(), "project-leap-logs")
    }
    
    @Primary
    @Bean(name = ["logsMongoTemplate"])
    fun logsMongoTemplate(): MongoTemplate {
        return MongoTemplate(logsMongoDatabaseFactory())
    }
}

@Configuration
@EnableMongoRepositories(
    basePackages = ["com.projectleap.repository.metadata"],
    mongoTemplateRef = "metadataMongoTemplate"
)
class MetadataMongoConfig {
    
    @Value("\${mongodb.metadata.uri}")
    private lateinit var metadataMongoUri: String
    
    @Bean(name = ["metadataMongoClient"])
    fun metadataMongoClient(): MongoClient {
        return MongoClients.create(metadataMongoUri)
    }
    
    @Bean(name = ["metadataMongoDatabaseFactory"])
    fun metadataMongoDatabaseFactory(): MongoDatabaseFactory {
        return SimpleMongoClientDatabaseFactory(metadataMongoClient(), "project-leap-metadata")
    }
    
    @Bean(name = ["metadataMongoTemplate"])
    fun metadataMongoTemplate(): MongoTemplate {
        return MongoTemplate(metadataMongoDatabaseFactory())
    }
}
