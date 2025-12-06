package com.projectleap.collector.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory

@Configuration
@EnableConfigurationProperties(MongoProps::class)
class MongoConfig {

    @Bean
    @Qualifier("logsMongoFactory")
    @Primary
    fun logsMongoFactory(props: MongoProps): MongoDatabaseFactory =
        SimpleMongoClientDatabaseFactory(ConnectionString(props.logs.uri))

    @Bean
    @Qualifier("metaMongoFactory")
    fun metaMongoFactory(props: MongoProps): MongoDatabaseFactory =
        SimpleMongoClientDatabaseFactory(ConnectionString(props.meta.uri))

    @Bean
    @Qualifier("logsTemplate")
    @Primary
    fun logsTemplate(@Qualifier("logsMongoFactory") factory: MongoDatabaseFactory) = MongoTemplate(factory)

    @Bean
    @Qualifier("metaTemplate")
    fun metaTemplate(@Qualifier("metaMongoFactory") factory: MongoDatabaseFactory) = MongoTemplate(factory)

    @Bean
    @Qualifier("logsTxManager")
    fun logsTxManager(@Qualifier("logsMongoFactory") factory: MongoDatabaseFactory) = MongoTransactionManager(factory)

    @Bean
    @Qualifier("metaTxManager")
    fun metaTxManager(@Qualifier("metaMongoFactory") factory: MongoDatabaseFactory) = MongoTransactionManager(factory)
}

@ConfigurationProperties(prefix = "mongo")
data class MongoProps(
    val logs: MongoInstance = MongoInstance(),
    val meta: MongoInstance = MongoInstance()
) {
    data class MongoInstance(
        var uri: String = "mongodb://localhost:27017/logs-db"
    )
}
