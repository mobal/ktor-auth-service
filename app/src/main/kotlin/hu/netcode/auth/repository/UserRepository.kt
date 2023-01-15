package hu.netcode.auth.repository

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemUtils
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.QueryRequest
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import hu.netcode.auth.model.Role
import hu.netcode.auth.model.User
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UserNotFoundException(message: String) : Exception(message)

class UserTypeConverter : DynamoDBTypeConverter<Map<String, AttributeValue>, User> {
    override fun convert(`object`: User): Map<String, AttributeValue> {
        val item = Item().withString("id", `object`.id)
            .withString("displayName", `object`.displayName)
            .withString("email", `object`.email)
            .withString("password", `object`.password)
            .withList("roles", `object`.roles)
            .withString("username", `object`.username)
            .withString("createdAt", `object`.createdAt.toString())
        `object`.updatedAt?.let {
            item.withString("updatedAt", `object`.updatedAt.toString())
        } ?: {
            item.withNull("updatedAt")
        }
        `object`.deletedAt?.let {
            item.withString("deletedAt", `object`.deletedAt.toString())
        } ?: {
            item.withNull("deletedAt")
        }
        return ItemUtils.toAttributeValues(item)
    }

    override fun unconvert(`object`: Map<String, AttributeValue>?): User {
        return User(
            `object`?.get("id")!!.s,
            `object`["display_name"]!!.s,
            `object`["email"]!!.s,
            `object`["password"]!!.s,
            `object`["roles"]!!.l.map { Role.values().first { role -> role.role == it.s } },
            `object`["username"]!!.s,
            Instant.parse(`object`["created_at"]!!.s).toLocalDateTime(TimeZone.UTC),
            if (`object`["deleted_at"]!!.isNULL == null)
                Instant.parse(`object`["deleted_at"]!!.s).toLocalDateTime(TimeZone.UTC) else null,
            if (`object`["updated_at"]!!.isNULL == null)
                Instant.parse(`object`["updated_at"]!!.s).toLocalDateTime(TimeZone.UTC) else null,
        )
    }
}

class UserRepository(
    private val stage: String,
    private val userTypeConverter: UserTypeConverter
) {
    private val ddb = AmazonDynamoDBAsyncClientBuilder.defaultClient()
    private val logger: Logger = LoggerFactory.getLogger(UserRepository::class.java)

    fun getByEmail(email: String): User? {
        val result = ddb.scan(
            ScanRequest()
                .withTableName("$stage-users")
                .withFilterExpression("#DELETED_AT = :deletedAt AND #EMAIL = :email")
                .withExpressionAttributeNames(mapOf("#DELETED_AT" to "deleted_at", "#EMAIL" to "email"))
                .withExpressionAttributeValues(
                    mapOf(
                        ":deletedAt" to AttributeValue().withNULL(true),
                        ":email" to AttributeValue(email)
                    )
                )
        )
        if (result.count > 0) {
            return userTypeConverter.unconvert(result.items.first())
        }
        return null
    }

    fun getById(id: String): User? {
        val result = ddb.query(
            QueryRequest()
                .withTableName("$stage-users")
                .withKeyConditionExpression("#ID = :id")
                .withExpressionAttributeNames(mapOf("#ID" to "id"))
                .withExpressionAttributeValues(mapOf(":id" to AttributeValue(id)))
        )
        if (result.count > 0) {
            return userTypeConverter.unconvert(result.items.first())
        }
        return null
    }
}
