package hu.netcode.auth.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import hu.netcode.auth.model.User
import hu.netcode.auth.repository.UserNotFoundException
import hu.netcode.auth.repository.UserRepository
import java.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder

class UnauthorizedException(message: String = "Unauthorized") : Exception(message)

class AuthService(
    private val argon2PasswordEncoder: Argon2PasswordEncoder,
    private val cacheService: CacheService,
    private val jwtSecret: String,
    private val userRepository: UserRepository,
) {
    private val logger: Logger = LoggerFactory.getLogger(AuthService::class.java)

    private fun generateToken(subject: String): String {
        val iat = Clock.System.now()
        val exp = iat.plus(1, DateTimeUnit.HOUR)
        return JWT.create()
            .withIssuedAt(Date(iat.toEpochMilliseconds()))
            .withExpiresAt(Date(exp.toEpochMilliseconds()))
            .withSubject(subject)
            .withJWTId(UUID.randomUUID().toString())
            .sign(Algorithm.HMAC256(jwtSecret))
    }

    private suspend fun validateToken(decodedJWT: DecodedJWT): Boolean {
        return (Clock.System.now().toEpochMilliseconds() < decodedJWT.expiresAt.time) &&
            !cacheService.get("jti_${decodedJWT.id}")
    }

    fun login(
        email: String,
        password: String,
    ): String {
        val user: User? = userRepository.getByEmail(email)
        return user?.let {
            if (!argon2PasswordEncoder.matches(password, it.password)) {
                throw UnauthorizedException()
            }
            return generateToken(Json.encodeToString(it))
        } ?: throw UserNotFoundException("User was not found with $email")
    }

    suspend fun logout(token: String) {
        val decodedJWT: DecodedJWT = JWT.decode(token.substring(token.indexOf(' ') + 1))
        if (validateToken(decodedJWT)) {
            cacheService.put(
                "jti_${decodedJWT.id}",
                decodedJWT.subject,
                (decodedJWT.expiresAt.time / 1000).toInt(),
            )
        } else {
            throw UnauthorizedException()
        }
    }
}
