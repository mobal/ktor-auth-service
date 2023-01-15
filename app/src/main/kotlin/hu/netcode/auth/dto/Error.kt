package hu.netcode.auth.dto

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Error(
        val id: String = UUID.randomUUID().toString(),
        val message: String,
        val status: Int? = null
)
