package hu.netcode.auth

import hu.netcode.auth.service.AuthService
import hu.netcode.auth.service.CacheService
import io.ktor.client.engine.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(Authentication)
    install(CallId)
    install(CallLogging) {
        level = Level.DEBUG
    }
    install(Koin) {
        slf4jLogger()
        org.koin.dsl.module {
            single {
                AuthService()
                CacheService()
            }
        }
    }
    install(MicrometerMetrics)
    routing {
        route("/api/v1") {
            val authService: AuthService by inject<AuthService>()

            route("/login") {
                post {
                    call.respondText("/api/v1/login")
                }
            }
            route("/logout") {
                val cacheService: CacheService by inject<CacheService>()

                post {
                    call.respondText("/api/v1/logout")
                }
            }
        }
    }
}
