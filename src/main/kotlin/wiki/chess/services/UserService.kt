package wiki.chess.services

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.bearerAuthorization
import wiki.chess.db
import wiki.chess.discordApi
import wiki.chess.enums.Role
import wiki.chess.models.AccessToken
import wiki.chess.models.DiscordError
import wiki.chess.models.DiscordUser
import wiki.chess.models.User

object UserService {
    private const val collectionName = "users"

    suspend fun getDiscordUserByToken(token: String): DiscordUser {
        val res = discordApi.get("users/@me") {
            bearerAuthorization(token)
        }

        if (!res.status.isSuccess()) {
            return DiscordUser(error = res.body<DiscordError>().apply { status = res.status })
        }

        return res.body()
    }

    suspend fun getUserById(id: String): User? {
        val user = withContext(Dispatchers.IO) {
            db.collection(collectionName).document(id).get().get()
        }.toObject(User::class.java) ?: return null

        user.id = id
        user.notifications = mapOf()

        return user
    }

    suspend fun getUserByToken(token: String): User? {
        val discordUser = getDiscordUserByToken(token)

        return getUserById(discordUser.id)
    }

    suspend fun getUsers(limit: Int, before: String): Map<String, User> {
        return GeneralService.get(collectionName, limit, before) { user ->
            user.toObject(User::class.java).apply {
                email = ""
                notifications = mapOf()
            }
        }
    }

    fun updateUser(user: User, data: Map<String, Any?>) {
        db.collection(collectionName).document(user.id).update(data)
    }

    fun deleteUser(user: User) {
        db.collection(collectionName).document(user.id).delete()
    }

    suspend fun initializeUser(accessToken: AccessToken) {
        val discordUser = getDiscordUserByToken(accessToken.access_token)

        val usersCollection = db.collection(collectionName)

        val user = withContext(Dispatchers.IO) {
            usersCollection
                .document(discordUser.id)
                .get().get()
        }

        if (!user.exists()) {
            val data: Map<String, Any?> = mapOf(
                "name" to discordUser.username,
                "bio" to "Look at me, I'm new!",
                "references" to ArrayList<String>(),
                "country" to null,
                "email" to "",
                "federation" to null,
                "rating" to 1000,
                "role" to Role.USER.name,
                "sex" to null,
                "title" to null,
                "notifications" to mapOf<String, String>(),
                "birthday" to 0,
                "registered_at" to System.currentTimeMillis() / 1000L
            )
            usersCollection.document(discordUser.id).set(data)
        }
    }
}
