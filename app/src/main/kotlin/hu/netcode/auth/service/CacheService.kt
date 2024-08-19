package hu.netcode.auth.service

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Serializable
data class CacheRequest(
    val key: String,
    val value: String,
    val ttl: Int,
)

class CacheService(
    private val baseUrl: String,
) {
    private val httpClient =
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
            install(Logging)
        }
    private val logger: Logger = LoggerFactory.getLogger(CacheService::class.java)

    suspend fun get(key: String): Boolean {
        val url = "$baseUrl/api/cache/$key"
        logger.debug("Get cache for $key, $url")
        val response = httpClient.get(url)
        if (response.status.value in 200..299) {
            return true
        } else if (response.status.value == HttpStatusCode.NotFound.value) {
            logger.debug("Cache was not found for $key")
            return false
        }
        logger.error("Unexpected error $response")
        throw ResponseException(response, "Internal Server Error")
    }

    suspend fun put(
        key: String,
        value: String,
        ttl: Int = 0,
    ): Boolean {
        val response =
            httpClient.post("$baseUrl/api/cache") {
                contentType(ContentType.Application.Json)
                setBody(CacheRequest(key, value, ttl))
            }
        if (response.status.value == HttpStatusCode.Created.value) {
            logger.info("Cache successfully created $key, $value and $ttl")
            return true
        } else {
            logger.error("Failed to put cache $key, $value and $ttl")
            throw ResponseException(response, "Internal Server Error")
        }
    }
}
