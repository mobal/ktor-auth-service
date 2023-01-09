plugins {
    id("com.google.devtools.ksp") version "1.8.0-1.0.8"
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    application
    idea
}

apply {
    plugin("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.insert-koin:koin-ktor:3.3.0")
    implementation("io.insert-koin:koin-logger-slf4j:3.3.0")
    implementation("io.ktor:ktor-server-auth:2.2.2")
    implementation("io.ktor:ktor-server-auth-jwt:2.2.2")
    implementation("io.ktor:ktor-server-call-id:2.2.2")
    implementation("io.ktor:ktor-server-call-logging:2.2.2")
    implementation("io.ktor:ktor-server-config-yaml:2.2.2")
    implementation("io.ktor:ktor-server-core:2.2.2")
    implementation("io.ktor:ktor-server-netty:2.2.2")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.2.2")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0'")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
    ksp("io.insert-koin:koin-ksp-compiler:1.1.0")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

ktlint {
    disabledRules.set(setOf("no-wildcard-imports"))
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}
