package hu.netcode.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable
import org.hibernate.validator.constraints.Length

@Serializable
data class Login(
    @field:NotBlank
    @field:Email
    val email: String? = null,
    @field:NotBlank
    @field:Length(min = 3)
    val password: String? = null,
)
