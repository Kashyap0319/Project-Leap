package com.projectleap.collector.config

import com.mongodb.ConnectionString
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager

@Configuration
class MongoConfig {
    
    @Bean
    @Primary
    fun logsMongoDatabaseFactory(
        @Value("\${mongo.logs.uri}") logsUri: String
    ): MongoDatabaseFactory {
        val connectionString = ConnectionString(logsUri)
        return SimpleMongoClientDatabaseFactory(connectionString)
    }
    
    @Bean
    @Primary
    fun logsMongoTemplate(
        @Qualifier("logsMongoDatabaseFactory") logsFactory: MongoDatabaseFactory
    ): MongoTemplate {
        return MongoTemplate(logsFactory)
    }
    
    @Bean
    fun logsTransactionManager(
        @Qualifier("logsMongoDatabaseFactory") logsFactory: MongoDatabaseFactory
    ): MongoTransactionManager {
        return MongoTransactionManager(logsFactory)
    }
    
    @Bean
    fun metaMongoDatabaseFactory(
        @Value("\${mongo.meta.uri}") metaUri: String
    ): MongoDatabaseFactory {
        val connectionString = ConnectionString(metaUri)
        return SimpleMongoClientDatabaseFactory(connectionString)
    }
    
    @Bean
    fun metaMongoTemplate(
        @Qualifier("metaMongoDatabaseFactory") metaFactory: MongoDatabaseFactory
    ): MongoTemplate {
        return MongoTemplate(metaFactory)
    }
    
    @Bean
    fun metaTransactionManager(
        @Qualifier("metaMongoDatabaseFactory") metaFactory: MongoDatabaseFactory
    ): MongoTransactionManager {
        return MongoTransactionManager(metaFactory)
    }
}

