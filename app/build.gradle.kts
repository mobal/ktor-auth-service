plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    application
    idea
    jacoco
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.amazonaws:aws-java-sdk:1.12.384")
    implementation("io.insert-koin:koin-ktor:3.3.0")
    implementation("io.insert-koin:koin-logger-slf4j:3.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.2.2")
    implementation("io.ktor:ktor-client-logging:2.2.2")
    implementation("io.ktor:ktor-client-okhttp:2.2.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.2")
    implementation("io.ktor:ktor-server-auth:2.2.2")
    implementation("io.ktor:ktor-server-auth-jwt:2.2.2")
    implementation("io.ktor:ktor-server-call-id:2.2.2")
    implementation("io.ktor:ktor-server-call-logging:2.2.2")
    implementation("io.ktor:ktor-server-config-yaml:2.2.2")
    implementation("io.ktor:ktor-server-content-negotiation:2.2.2")
    implementation("io.ktor:ktor-server-core:2.2.2")
    implementation("io.ktor:ktor-server-netty:2.2.2")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.2.2")
    implementation("io.ktor:ktor-server-request-validation:2.2.2")
    implementation("io.ktor:ktor-server-status-pages:2.2.2")
    implementation("io.kotless:kotless-lang:0.2.0")
    implementation("io.kotless:kotless-lang-aws:0.2.0")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0'")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.72")
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("org.glassfish.expressly:expressly:5.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.springframework.security:spring-security-crypto:6.0.1")
    testImplementation("io.insert-koin:koin-test:3.3.2")
    testImplementation("io.ktor:ktor-server-test-host:2.2.2")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

jacoco {
    toolVersion = "0.8.8"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

ktlint {
    disabledRules.set(setOf("no-wildcard-imports"))
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}
