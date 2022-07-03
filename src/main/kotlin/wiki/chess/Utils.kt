package wiki.chess

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import wiki.chess.enums.Errors
import wiki.chess.models.DiscordError
import wiki.chess.models.DiscordUser
import wiki.chess.models.User
import wiki.chess.resources.Users
import wiki.chess.services.UserService

suspend fun getDiscordUser(call: ApplicationCall): DiscordUser? {
    val token = call.request.headers[HttpHeaders.Authorization]
        .validateIsNull(call, Errors.AUTH_HEADER) ?: return null

    val res = discordApi.get(Users.Me()) {
        bearerAuthorization(token)
    }

    if (!res.status.isSuccess()) {
        call.respond(HttpStatusCode.fromValue(res.status.value), res.body<DiscordError>())
        return null
    }

    return res.body()
}

suspend fun getUser(call: ApplicationCall, id: String): User? {
    val user = UserService.getUserById(id).validateIsNull(call, Errors.USER_NOT_FOUND) ?: return null

    user.id = id.toLong()

    return user
}

suspend fun getUser(call: ApplicationCall): User? {
    val discordUser = getDiscordUser(call) ?: return null

    return getUser(call, discordUser.id)
}

fun HttpMessageBuilder.bearerAuthorization(token: String) {
    this.headers.append(HttpHeaders.Authorization, "Bearer $token")
}

val discordApi = HttpClient {
    defaultRequest {
        url("https://discord.com/api/v10/")
    }
    install(Resources)
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}
