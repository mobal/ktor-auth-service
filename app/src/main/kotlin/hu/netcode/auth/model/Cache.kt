package hu.netcode.auth.model

import java.time.LocalDateTime

data class Cache(
    val key: String,
    val value: String,
    val createdAt: LocalDateTime,
    val ttl: Int
)
