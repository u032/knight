package wiki.chess.services

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import wiki.chess.config
import wiki.chess.discordApi
import wiki.chess.models.AccessToken
import wiki.chess.models.DiscordError

object TokenService {
    suspend fun getToken(call: ApplicationCall, code: String): AccessToken? {
        val res = discordApi.post("oauth2/token") {
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

    suspend fun refreshToken(call: ApplicationCall, token: String): AccessToken? {
        val res = discordApi.post("oauth2/token") {
            setBody(FormDataContent(Parameters.build {
                append("client_id", config["DISCORD_ID"])
                append("client_secret", config["DISCORD_SECRET"])
                append("grant_type", "refresh_token")
                append("refresh_token", token)
            }))
            contentType(ContentType.Application.FormUrlEncoded)
        }

        if (!res.status.isSuccess()) {
            call.respond(HttpStatusCode.fromValue(res.status.value), res.body<DiscordError>())
            return null
        }

        return res.body()
    }

    suspend fun revokeToken(call: ApplicationCall, token: String): String? {
        val res = discordApi.post("oauth2/token/revoke") {
            setBody(FormDataContent(Parameters.build {
                append("client_id", config["DISCORD_ID"])
                append("client_secret", config["DISCORD_SECRET"])
                append("token", token)
            }))
            contentType(ContentType.Application.FormUrlEncoded)
        }

        if (!res.status.isSuccess()) {
            call.respond(HttpStatusCode.fromValue(res.status.value), res.body<DiscordError>())
            return null
        }

        return "Token revoked"
    }
}
