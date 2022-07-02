package wiki.chess.services

import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.bearerAuthorization
import wiki.chess.config
import wiki.chess.db
import wiki.chess.discordApi
import wiki.chess.enums.Country
import wiki.chess.enums.Role
import wiki.chess.models.AccessToken
import wiki.chess.models.DiscordError
import wiki.chess.models.DiscordUser
import wiki.chess.resources.OAuth2
import wiki.chess.resources.Users

object TokenService {
    suspend fun getToken(call: ApplicationCall): AccessToken? {
        val code = call.request.queryParameters["code"]
        if (code.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Parameter 'code' is missing")
            return null
        }

        val res = discordApi.post(OAuth2.Token()) {
            setBody(FormDataContent(Parameters.build {
                append("client_id", config["DISCORD_ID"])
                append("client_secret", config["DISCORD_SECRET"])
                append("grant_type", "authorization_code")
                append("code", code)
                append("redirect_uri", config["FRONTEND_CALLBACK_URL"])
            }))
            contentType(ContentType.Application.FormUrlEncoded)
        }

        if (!res.status.value.toString().startsWith("2")) {
            call.respond(HttpStatusCode.fromValue(res.status.value), res.body<DiscordError>())
            return null
        }

        return res.body()
    }

    suspend fun refreshToken(call: ApplicationCall): AccessToken? {
        val token = call.request.queryParameters["token"]
        if (token.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Parameter 'token' is missing")
            return null
        }

        val res = discordApi.post(OAuth2.Token()) {
            setBody(FormDataContent(Parameters.build {
                append("client_id", config["DISCORD_ID"])
                append("client_secret", config["DISCORD_SECRET"])
                append("grant_type", "refresh_token")
                append("refresh_token", token)
            }))
            contentType(ContentType.Application.FormUrlEncoded)
        }

        if (!res.status.value.toString().startsWith("2")) {
            call.respond(HttpStatusCode.fromValue(res.status.value), res.body<DiscordError>())
            return null
        }

        return res.body()
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
            val data: Map<String, Any> = mapOf(
                "name" to discordUser.username,
                "bio" to "Look at me, I'm new!",
                "references" to ArrayList<String>(),
                "country" to Country.UN.name,
                "email" to "",
                "federation" to "",
                "rating" to 0,
                "role" to Role.USER.name,
                "sex" to "",
                "title" to ""
            )
            usersCollection.document(discordUser.id).set(data)
        }
    }
}
