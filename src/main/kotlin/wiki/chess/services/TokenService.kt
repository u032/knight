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
    /**
     * It takes a code from the URL, sends it to Discord, and returns the access token
     *
     * @param call The call that was made to the server.
     * @param code The code that was sent to the redirect URI
     * @return An AccessToken object
     */
    suspend fun getToken(call: ApplicationCall, code: String): AccessToken? {
        val res = discordApi.post("oauth2/token") {
            setBody(FormDataContent(Parameters.build {
                append("client_id", config["DISCORD_ID"])
                append("client_secret", config["DISCORD_SECRET"])
                append("grant_type", "authorization_code")
                append("code", code)
                append("redirect_uri", config["REDIRECT_URI"])
            }))
            contentType(ContentType.Application.FormUrlEncoded)
        }

        if (!res.status.value.toString().startsWith("2")) {
            call.respond(HttpStatusCode.fromValue(res.status.value), res.body<DiscordError>())
            return null
        }

        return res.body()
    }

    /**
     * It takes a call and a refresh token, and returns the access token
     *
     * @param call The call that is being made to the server.
     * @param token The refresh token that was given to the user when they logged in.
     * @return An AccessToken object
     */
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

    /**
     * It takes a call and a token, and revokes the token
     *
     * @param call The call that was made to the server.
     * @param token The token you want to revoke
     * @return A string
     */
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
