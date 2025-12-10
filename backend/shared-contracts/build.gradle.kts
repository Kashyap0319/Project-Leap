plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.23"
}

group = "com.projectleap"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.test {
    useJUnitPlatform()
}

