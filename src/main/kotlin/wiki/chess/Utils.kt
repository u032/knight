package wiki.chess

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import wiki.chess.models.DiscordUser

suspend fun getDiscordUser(call: ApplicationCall): DiscordUser? {
    val token = call.request.headers[HttpHeaders.Authorization]
    if (token == null) {
        call.respond(HttpStatusCode.Unauthorized, "401 Unauthorized")
        return null
    }

    return httpClient.get("https://discord.com/api/users/@me") {
        headers {
            append(HttpHeaders.Authorization, "Bearer $token")
        }
    }.body()
}
