package wiki.chess

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import wiki.chess.models.DiscordUser
import wiki.chess.models.Post
import wiki.chess.models.User
import wiki.chess.services.PostService
import wiki.chess.services.UserService

fun HttpMessageBuilder.authorization(token: String) {
    this.headers.append(HttpHeaders.Authorization, token)
}

fun HttpMessageBuilder.bearerAuthorization(token: String) {
    authorization("Bearer $token")
}

suspend fun ApplicationCall.getToken(): String? {
    return request.headers[HttpHeaders.Authorization]
        .validateIsNull(this, HttpStatusCode.BadRequest, "Authorization header missing")
}

suspend fun ApplicationCall.getPath(parameter: String): String? {
    return parameters[parameter]
        .validateIsNull(this, HttpStatusCode.BadRequest, "Path parameter $parameter missing")
}

suspend fun ApplicationCall.getQuery(parameter: String, required: Boolean = true): String? {
    return if (required) {
        request.queryParameters[parameter] ?: ""
    } else {
        request.queryParameters[parameter]
            .validateIsNull(this, HttpStatusCode.BadRequest, "Query parameter $parameter missing")
    }
}

suspend fun ApplicationCall.getDiscordUser(): DiscordUser? {
    val token = getToken() ?: return null

    val user = UserService.getDiscordUserByToken(token)

    if (user.error != null) {
        respond(user.error.status, user.error)
        return null
    }

    return user
}

suspend fun ApplicationCall.getUser(): User? {
    val discordUser = getDiscordUser() ?: return null

    val user = UserService.getUserById(discordUser.id)

    if (user == null) {
        respond(HttpStatusCode.NotFound, "User not found")
        return null
    }

    return user
}

suspend fun ApplicationCall.getUser(parameter: String): User? {
    val id = getPath(parameter) ?: return null

    val user = UserService.getUserById(id)

    if (user == null) {
        respond(HttpStatusCode.NotFound, "User not found")
        return null
    }

    return user
}

suspend fun ApplicationCall.getPost(parameter: String): Post? {
    val id = getPath(parameter) ?: return null

    val post = PostService.getPostById(id)

    if (post == null) {
        respond(HttpStatusCode.NotFound, "Post not found")
        return null
    }

    return post
}

val discordApi = HttpClient {
    defaultRequest {
        url("https://discord.com/api/v10/")
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}
