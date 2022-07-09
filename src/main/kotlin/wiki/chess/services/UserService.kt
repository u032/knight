package wiki.chess.services

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.bearerAuthorization
import wiki.chess.db
import wiki.chess.discordApi
import wiki.chess.enums.HttpError
import wiki.chess.enums.Role
import wiki.chess.models.AccessToken
import wiki.chess.models.DiscordError
import wiki.chess.models.DiscordUser
import wiki.chess.models.User
import wiki.chess.validateIsNull

object UserService {
    private const val collectionName = "users"

    private suspend fun getDiscordUserByToken(token: String): DiscordUser? {
        val res = discordApi.get("users/@me") {
            bearerAuthorization(token)
        }

        if (!res.status.isSuccess()) return null

        return res.body()
    }

    suspend fun getDiscordUser(call: ApplicationCall): DiscordUser? {
        val token = call.request.headers[HttpHeaders.Authorization]
            .validateIsNull(call, HttpError.AUTH_HEADER) ?: return null

        val res = discordApi.get("users/@me") {
            bearerAuthorization(token)
        }

        if (!res.status.isSuccess()) {
            call.respond(HttpStatusCode.fromValue(res.status.value), res.body<DiscordError>())
            return null
        }

        return res.body()
    }

    private suspend fun getUser(id: String): User? {
        val user = withContext(Dispatchers.IO) {
            db.collection(collectionName).document(id).get().get()
        }.toObject(User::class.java) ?: return null

        user.id = id
        user.notifications = mapOf()

        return user
    }

    suspend fun getUserById(call: ApplicationCall, id: String): User? {
        return getUser(id).validateIsNull(call, HttpError.USER_NOT_FOUND)
    }

    suspend fun getUser(call: ApplicationCall): User? {
        val discordUser = getDiscordUser(call) ?: return null

        return getUserById(call, discordUser.id)
    }

    suspend fun getUserByToken(token: String): User? {
        val discordUser = getDiscordUserByToken(token) ?: return null

        return getUser(discordUser.id)
    }

    suspend fun getAllUsersSafety(): Map<String, User> {
        val users: MutableMap<String, User> = mutableMapOf()

        withContext(Dispatchers.IO) {
            db.collection(collectionName).get().get().documents
        }.forEach { user ->
            users[user.id] = user.toObject(User::class.java).apply {
                email = ""
                notifications = mapOf()
            }
        }

        return users
    }

    fun deleteUserById(user: DiscordUser) {
        db.collection(collectionName).document(user.id).delete()
    }

    suspend fun initializeUser(accessToken: AccessToken) {
        val discordUser = getDiscordUserByToken(accessToken.access_token)!!

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
                "rating" to 0,
                "role" to Role.USER.name,
                "sex" to null,
                "title" to null,
                "notifications" to mapOf<String, String>()
            )
            usersCollection.document(discordUser.id).set(data)
        }
    }
}
