package wiki.chess.services

import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.bearerAuthorization
import wiki.chess.db
import wiki.chess.discordApi
import wiki.chess.enums.Country
import wiki.chess.enums.Role
import wiki.chess.models.AccessToken
import wiki.chess.models.DiscordUser
import wiki.chess.models.User
import wiki.chess.resources.Users

object UserService {
    suspend fun getAllUsersSafety(): List<User> {
        val usersDocuments = withContext(Dispatchers.IO) {
            db.collection("users").get().get().documents
        }

        val users: ArrayList<User> = ArrayList()

        usersDocuments.forEach { user ->
            users.add(user.toObject(User::class.java).apply { email = "" })
        }

        return users
    }

    suspend fun initializeUser(accessToken: AccessToken) {
        val discordUser: DiscordUser = discordApi.get(Users.Me()) {
            bearerAuthorization(accessToken.access_token)
        }.body()

        val usersCollection = db.collection("users")

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
                "country" to Country.UN.name,
                "email" to "",
                "federation" to null,
                "rating" to 0,
                "role" to Role.USER.name,
                "sex" to null,
                "title" to null
            )
            usersCollection.document(discordUser.id).set(data)
        }
    }
}