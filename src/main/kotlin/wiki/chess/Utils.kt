package wiki.chess

import com.google.cloud.firestore.QueryDocumentSnapshot
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
        .isNull(this, HttpStatusCode.BadRequest, "Authorization header missing")
}

suspend fun ApplicationCall.getPath(parameter: String): String? {
    return parameters[parameter]
        .isNull(this, HttpStatusCode.BadRequest, "Path parameter $parameter missing")
}

suspend fun ApplicationCall.getQuery(parameter: String, required: Boolean = true): String? {
    return if (required) {
        request.queryParameters[parameter]
            .isNull(this, HttpStatusCode.BadRequest, "Query parameter $parameter missing")
    } else {
        request.queryParameters[parameter] ?: ""
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

suspend fun String.toInt(call: ApplicationCall): Int? {
    val num = toIntOrNull()
    if (num == null) {
        call.respond(HttpStatusCode.BadRequest, "Cannot parse '$this' to integer")
        return null
    }
    return num
}

suspend fun String.toBoolean(call: ApplicationCall): Boolean? {
    val bool = toBooleanStrictOrNull()
    if (bool == null) {
        call.respond(HttpStatusCode.BadRequest, "Cannot parse '$this' to boolean")
        return null
    }
    return bool
}

fun currentTime(): Long {
    return System.currentTimeMillis() / 1000L
}

fun QueryDocumentSnapshot.toUser(): User {
    val documentId = this.id
    return toObject(User::class.java).apply {
        id = documentId
    }
}

fun QueryDocumentSnapshot.toPost(): Post {
    val documentId = this.id
    return toObject(Post::class.java).apply {
        id = documentId
    }
}

val discordApi = HttpClient {
    defaultRequest {
        url(config["DISCORD_API"])
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}
