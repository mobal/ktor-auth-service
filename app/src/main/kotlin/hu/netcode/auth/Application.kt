package hu.netcode.auth

import hu.netcode.auth.repository.UserNotFoundException
import hu.netcode.auth.repository.UserRepository
import hu.netcode.auth.repository.UserTypeConverter
import hu.netcode.auth.service.AuthService
import hu.netcode.auth.service.CacheService
import hu.netcode.auth.service.UnauthorizedException
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jakarta.validation.Validation
import jakarta.validation.Validator
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import hu.netcode.auth.dto.Error as ErrorDto
import hu.netcode.auth.dto.Login as LoginDto

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(Authentication)
    install(CallId)
    install(CallLogging)
    install(ContentNegotiation) {
        json(
            Json {
                isLenient = true
                prettyPrint = true
            }
        )
    }
    install(Koin) {
        slf4jLogger(level = Level.DEBUG)
        modules(
            module {
                single { Argon2PasswordEncoder(16, 32, 4, 65536, 3) }
                single { AuthService(get(), get(), environment.config.property("jwt.secret").getString(), get()) }
                single { CacheService(environment.config.property("cache.baseUrl").getString()) }
                single { UserRepository(environment.config.property("aws.stage").getString(), get()) }
                single { UserTypeConverter() }
                single { Validation.buildDefaultValidatorFactory().validator }
            }
        )
    }
    install(RequestValidation) {
        val validator by inject<Validator>()
        validate<LoginDto> {
            validator.validate(it).takeIf { violations -> violations.isNotEmpty() }
                ?.let { violations ->
                    ValidationResult.Invalid(violations.map { violation -> violation.message })
                }
                ?: ValidationResult.Valid
        }
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is BadRequestException -> {
                    call.respondText(
                        text = Json.encodeToString(
                            ErrorDto(message = cause.let { cause.message } ?: "Bad Request")
                        ),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.BadRequest
                    )
                }
                is RequestValidationException -> {
                    call.respondText(
                        text = Json.encodeToString(
                            ErrorDto(message = cause.reasons.joinToString())
                        ),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.BadRequest
                    )
                }
                is UnauthorizedException -> {
                    call.respondText(
                        text = Json.encodeToString(
                            ErrorDto(message = cause.let { cause.message } ?: "Unauthorized")
                        ),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.Unauthorized
                    )
                }
                is UserNotFoundException -> {
                    call.respondText(
                        text = Json.encodeToString(
                            ErrorDto(message = cause.let { cause.message } ?: "Not Found")
                        ),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.NotFound
                    )
                }
                else -> {
                    call.respondText(
                        text = Json.encodeToString(ErrorDto(message = "Internal Server Error")),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.InternalServerError,
                    )
                }
            }
        }
    }

    routing {
        route("/api/v1") {
            val service by inject<AuthService>()
            route("/login", HttpMethod.Post) {
                handle {
                    val login = call.receive<LoginDto>()
                    call.respond(mapOf("token" to service.login(login.email!!, login.password!!)))
                }
            }
            route("/logout", HttpMethod.Get) {
                handle {
                    call.request.headers["Authorization"]?.let {
                        service.logout(it)
                        call.response.status(value = HttpStatusCode.NoContent)
                    }
                }
            }
        }
    }
}
