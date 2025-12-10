package com.projectleap.collector.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(
    basePackages = ["com.projectleap.collector.logs.repository"],
    mongoTemplateRef = "logsMongoTemplate"
)
class LogsMongoConfig

