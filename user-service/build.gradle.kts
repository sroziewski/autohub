import java.io.FileInputStream
import java.util.Properties

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
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.flywaydb.flyway") version "9.16.0"
	id("com.github.gmazzo.buildconfig") version "3.1.0"
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
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-quartz")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.session:spring-session-data-redis")
	implementation("org.hibernate.orm:hibernate-spatial")
	implementation("org.locationtech.jts:jts-core")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("commons-validator:commons-validator:1.7")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
}

tasks.withType<Test> {
	useJUnitPlatform()
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
