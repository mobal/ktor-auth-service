plugins {
    id("com.gradleup.shadow") version "8.3.0"
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
    implementation("com.amazonaws:aws-java-sdk:1.12.769")
    implementation("io.insert-koin:koin-ktor:${project.property("koinVersion")}")
    implementation("io.insert-koin:koin-logger-slf4j:${project.property("koinVersion")}")
    implementation("io.ktor:ktor-client-content-negotiation:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-client-logging:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-client-okhttp:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-auth:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-auth-jwt:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-call-id:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-call-logging:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-config-yaml:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-content-negotiation:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-core:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-netty:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-metrics-micrometer:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-request-validation:${project.property("ktorVersion")}")
    implementation("io.ktor:ktor-server-status-pages:${project.property("ktorVersion")}")
    implementation("org.apache.logging.log4j:log4j-api:${project.property("log4jVersion")}")
    implementation("org.apache.logging.log4j:log4j-core:${project.property("log4jVersion")}")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:${project.property("log4jVersion")}")
    implementation("org.bouncycastle:bcprov-jdk18on:${project.property("bcpVersion")}")
    implementation("org.hibernate.validator:hibernate-validator:${project.property("hibernateValidatorVersion")}")
    implementation("org.glassfish.expressly:expressly:${project.property("expresslyVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${project.property("kotlinxDatetime")}")
    implementation("org.springframework.security:spring-security-crypto:${project.property("springSecurityVersion")}")
    testImplementation("io.insert-koin:koin-test:${project.property("koinVersion")}")
    testImplementation("io.ktor:ktor-server-test-host:${project.property("ktorVersion")}")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test:${project.property("kotlinVersion")}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${project.property("junitVersion")}")
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
        property("sonar.gradle.skipCompile", true)
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "mobal")
        property("sonar.projectKey", "mobal_ktor-auth-service")
        property("sonar.projectName", "ktor-auth-service")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    useJUnitPlatform()
}

tasks.shadowJar {
    isEnableRelocation = true
    isZip64 = true
    minimize()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_22)
    }
}
