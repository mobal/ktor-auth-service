package hu.netcode.auth.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object RoleSerializer : KSerializer<Role> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Role", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Role {
        return Role.entries.first {
            it.role == decoder.decodeString()
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: Role,
    ) {
        encoder.encodeString(value.role)
    }
}

@Serializable(with = RoleSerializer::class)
enum class Role(val role: String) {
    PostCreate("post:create"),
    PostDelete("post:delete"),
    PostEdit("post:edit"),
}

@Serializable
data class User(
    @Transient
    val id: String? = null,
    val displayName: String,
    val email: String,
    @Transient
    val password: String? = null,
    val roles: List<Role>,
    val username: String,
    @Contextual
    @Transient
    val createdAt: LocalDateTime? = null,
    @Contextual
    @Transient
    val deletedAt: LocalDateTime? = null,
    @Contextual
    @Transient
    val updatedAt: LocalDateTime? = null,
)
