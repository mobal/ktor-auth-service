plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.kotlin.jvm") version "2.0.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.10"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("org.sonarqube") version "4.4.1.3373"
    application
    idea
    jacoco
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.amazonaws:aws-java-sdk:latest.release")
    implementation("io.insert-koin:koin-ktor:latest.release")
    implementation("io.insert-koin:koin-logger-slf4j:latest.release")
    implementation("io.ktor:ktor-client-content-negotiation:latest.release")
    implementation("io.ktor:ktor-client-logging:latest.release")
    implementation("io.ktor:ktor-client-okhttp:latest.release")
    implementation("io.ktor:ktor-serialization-kotlinx-json:latest.release")
    implementation("io.ktor:ktor-server-auth:latest.release")
    implementation("io.ktor:ktor-server-auth-jwt:latest.release")
    implementation("io.ktor:ktor-server-call-id:latest.release")
    implementation("io.ktor:ktor-server-call-logging:latest.release")
    implementation("io.ktor:ktor-server-config-yaml:latest.release")
    implementation("io.ktor:ktor-server-content-negotiation:latest.release")
    implementation("io.ktor:ktor-server-core:latest.release")
    implementation("io.ktor:ktor-server-netty:latest.release")
    implementation("io.ktor:ktor-server-metrics-micrometer:latest.release")
    implementation("io.ktor:ktor-server-request-validation:latest.release")
    implementation("io.ktor:ktor-server-status-pages:latest.release")
    implementation("org.apache.logging.log4j:log4j-api:latest.release")
    implementation("org.apache.logging.log4j:log4j-core:latest.release")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:latest.release")
    implementation("org.bouncycastle:bcprov-jdk18on:latest.release")
    implementation("org.hibernate.validator:hibernate-validator:latest.release")
    implementation("org.glassfish.expressly:expressly:latest.release")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:latest.release")
    implementation("org.springframework.security:spring-security-crypto:latest.release")
    testImplementation("io.insert-koin:koin-test:latest.release")
    testImplementation("io.ktor:ktor-server-test-host:latest.release")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

jacoco {
    toolVersion = "0.8.12"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }
}

sonar {
    properties {
        property("sonar.projectKey", "mobal_ktor-auth-service")
        property("sonar.organization", "mobal")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    useJUnitPlatform()
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    minimize()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_22)
    }
}
