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
    suspend fun getToken(call: ApplicationCall): AccessToken? {
        val code = call.request.queryParameters["code"]
        if (code.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Parameter 'code' is missing")
            return null
        }

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

    suspend fun refreshToken(call: ApplicationCall): AccessToken? {
        val token = call.request.queryParameters["token"]
        if (token.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Parameter 'token' is missing")
            return null
        }

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
}
