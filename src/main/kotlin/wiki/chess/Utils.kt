package wiki.chess

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.models.DiscordUser
import wiki.chess.models.User

suspend fun getDiscordUser(call: ApplicationCall): DiscordUser? {
    val token = call.request.headers[HttpHeaders.Authorization]
    if (token == null) {
        call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
        return null
    }

    return httpClient.get("https://discord.com/api/users/@me") {
        headers {
            append(HttpHeaders.Authorization, "Bearer $token")
        }
    }.body()
}

suspend fun getUser(call: ApplicationCall, id: String): User? {
    val user = withContext(Dispatchers.IO) {
        db.collection("users").document(id).get().get()
    }.toObject(User::class.java)

    if (user == null) {
        call.respond(HttpStatusCode.NotFound, "User not found")
        return null
    }

    return user
}

suspend fun getUser(call: ApplicationCall): User? {
    val discordUser = getDiscordUser(call) ?: return null

    return getUser(call, discordUser.id)
}
