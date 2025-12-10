package com.projectleap.collector.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(
    basePackages = [
        "com.projectleap.collector.alerts.repository",
        "com.projectleap.collector.incidents.repository",
        "com.projectleap.collector.auth.repository"
    ],
    mongoTemplateRef = "metaMongoTemplate"
)
class MetaMongoConfig

