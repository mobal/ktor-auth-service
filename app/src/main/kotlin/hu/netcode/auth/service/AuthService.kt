package hu.netcode.auth.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import hu.netcode.auth.model.User
import hu.netcode.auth.repository.UserNotFoundException
import hu.netcode.auth.repository.UserRepository
import kotlinx.datetime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.util.*

class UnauthorizedException(message: String = "Unauthorized") : Exception(message)

class AuthService(
        private val argon2PasswordEncoder: Argon2PasswordEncoder,
        private val cacheService: CacheService,
        private val jwtSecret: String,
        private val userRepository: UserRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(AuthService::class.java)

    private suspend fun generateToken(subject: String): String {
        val iat = Clock.System.now()
        val exp = iat.plus(1, DateTimeUnit.HOUR)
        return JWT.create()
                .withIssuedAt(Date(iat.toEpochMilliseconds()))
                .withExpiresAt(Date(exp.toEpochMilliseconds()))
                .withSubject(subject)
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(jwtSecret))
    }

    suspend fun login(email: String, password: String): String {
        val user: User? = userRepository.getByEmail(email)
        return user?.let {
            if (!argon2PasswordEncoder.matches(password, it.password)) {
                throw UnauthorizedException()
            }
            return generateToken(Json.encodeToString(it))
        } ?: throw UserNotFoundException("User was not found with $email")
    }

    suspend fun logout(token: String) {
        val decodedToken = JWT.decode(token)
        cacheService.put("jti_${decodedToken.id}", decodedToken.subject,
                (decodedToken.expiresAt.time / 1000).toInt())
    }
}
