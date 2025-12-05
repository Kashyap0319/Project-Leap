package com.projectleap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableMongoAuditing
@EnableAsync
@EnableScheduling
class ProjectLeapApplication

fun main(args: Array<String>) {
    runApplication<ProjectLeapApplication>(*args)
}
