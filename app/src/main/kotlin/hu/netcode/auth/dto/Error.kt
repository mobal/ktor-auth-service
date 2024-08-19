package hu.netcode.auth.dto

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Error(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val status: Int? = null,
)
