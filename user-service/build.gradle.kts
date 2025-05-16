import java.io.FileInputStream
import java.util.*

val envProps = Properties()
val envFile = rootProject.file(".env")
if (envFile.exists()) {
    envProps.load(FileInputStream(envFile))
    println("Loaded .env file")
} else {
    println(".env file not found at ${envFile.absolutePath}")
}

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.flyway)
    alias(libs.plugins.buildconfig)
}

group = "com.autohub.user-service"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot core dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Database and persistence
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.hibernate.orm:hibernate-spatial")
    implementation("org.locationtech.jts:jts-core")
    runtimeOnly(libs.postgresql.driver)

    // Flyway database migration
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Redis and session management
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.session:spring-session-data-redis")

    // Message queue for asynchronous processing
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // Scheduled tasks and email
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Thymeleaf for email templates
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // Validation
    implementation(libs.commons.validator)

    // Security and JWT
    implementation(libs.bundles.jwt)

    // OAuth2 for social login
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // Rate limiting
    implementation(libs.bundles.bucket4j)

    // Circuit breaker
    implementation(libs.bundles.resilience4j)

    // Structured logging
    implementation(libs.bundles.logging)

    // AOP for logging aspect
    implementation(libs.bundles.aop)

    // TOTP for 2FA
    implementation(libs.bundles.totp)

    // MapStruct
    implementation(libs.bundles.mapstruct)
    annotationProcessor(libs.mapstruct.processor)

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor(libs.lombok.mapstruct.binding)

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(libs.bundles.testcontainers)
    testImplementation("org.mockito:mockito-subclass:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
}

// Use properties from .env file for Flyway configuration
flyway {
    url = envProps.getProperty("DB_URL")
    user = envProps.getProperty("DB_USERNAME")
    password = envProps.getProperty("DB_PASSWORD")
    baselineOnMigrate = true
    locations = arrayOf("classpath:db/migration")
    cleanDisabled = false
    schemas = arrayOf("autohub")
    defaultSchema = "autohub"
    createSchemas = true
}

tasks.register("printEnvProps") {
    doLast {
        println("Values from .env file:")
        println("DB_URL: ${envProps.getProperty("DB_URL") ?: "NOT FOUND"}")
        println("DB_USERNAME: ${envProps.getProperty("DB_USERNAME") ?: "NOT FOUND"}")
        println("DB_PASSWORD: ${envProps.getProperty("DB_PASSWORD") ?: "NOT FOUND"}")
        println("DB_NAME: ${envProps.getProperty("DB_NAME") ?: "NOT FOUND"}")
        println("DB_PORT: ${envProps.getProperty("DB_PORT") ?: "NOT FOUND"}")
        println("JWT_SECRET_KEY: ${envProps.getProperty("JWT_SECRET_KEY") ?: "NOT FOUND"}")
        println("JWT_VALIDITY: ${envProps.getProperty("JWT_VALIDITY") ?: "NOT FOUND"}")
    }
}

tasks.register("printProjectRoot") {
    doLast {
        println("Project Root: ${rootProject.projectDir}")
    }
}
