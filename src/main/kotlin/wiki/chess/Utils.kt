package wiki.chess

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import wiki.chess.models.DiscordUser
import wiki.chess.models.Post
import wiki.chess.models.User
import wiki.chess.services.PostService
import wiki.chess.services.UserService

/** It's a function that adds an authorization header to the request. */
fun HttpMessageBuilder.authorization(token: String) {
    this.headers.append(HttpHeaders.Authorization, token)
}

/** It's a function that adds an authorization header to the request. */
fun HttpMessageBuilder.bearerAuthorization(token: String) {
    authorization("Bearer $token")
}

/**
 * If the Authorization header is missing, return a 400 Bad Request error. Otherwise, return the value of the Authorization
 * header
 *
 * @return A String?
 */
suspend fun ApplicationCall.getToken(): String? {
    return request.headers[HttpHeaders.Authorization]
        .isNull(this, HttpStatusCode.BadRequest, "Authorization header missing")
}

/**
 * "If the parameter is missing, return a 400 Bad Request error, otherwise return the parameter."
 *
 * The function is a suspend function, which means it can be called from a coroutine. It takes a single parameter, which is
 * the name of the parameter to retrieve. It returns a String? (nullable String)
 *
 * @param parameter The name of the parameter to get.
 * @return A String?
 */
suspend fun ApplicationCall.getPath(parameter: String): String? {
    return parameters[parameter]
        .isNull(this, HttpStatusCode.BadRequest, "Path parameter $parameter missing")
}

/**
 * If the query parameter is missing, return a 400 error. Otherwise, return the query parameter
 *
 * @param parameter The name of the query parameter to get.
 * @param required If the parameter is required, then the function will return an error if it's not present.
 * @return A String?
 */
suspend fun ApplicationCall.getQuery(parameter: String, required: Boolean = true): String? {
    return if (required) {
        request.queryParameters[parameter]
            .isNull(this, HttpStatusCode.BadRequest, "Query parameter $parameter missing")
    } else {
        request.queryParameters[parameter] ?: ""
    }
}

/**
 * It returns the value of the form parameter with the given name, or returns a 400 Bad Request error if the parameter is
 * missing
 *
 * @param parameter The name of the form parameter to get.
 * @return A String?
 */
suspend fun ApplicationCall.getForm(parameter: String): String? {
    return receiveParameters()[parameter]
        .isNull(this, HttpStatusCode.BadRequest, "Form parameter $parameter missing")
}

/**
 * If the user is authenticated, return the user, otherwise return null
 *
 * @return A DiscordUser object
 */
suspend fun ApplicationCall.getDiscordUser(): DiscordUser? {
    val token = getToken() ?: return null

    val user = UserService.getDiscordUserByToken(token)

    if (user.error != null) {
        respond(user.error.status, user.error)
        return null
    }

    return user
}

/**
 * `getUser` gets the user from the database, or returns a 404 if the user doesn't exist
 *
 * @return A User object
 */
suspend fun ApplicationCall.getUser(): User? {
    val discordUser = getDiscordUser() ?: return null

    val user = UserService.getUserById(discordUser.id, false)

    if (user == null) {
        respond(HttpStatusCode.NotFound, "User not found")
        return null
    }

    return user
}

/**
 * "Get the user from the path parameter, if it exists, and if it doesn't, return a 404."
 *
 * The first line of the function is a call to the `getPath` function, which is a function that is available on the
 * `ApplicationCall` object. This function will return the value of the path parameter that is passed in. If the path
 * parameter is not present, it will return null
 *
 * @param parameter The name of the parameter in the path.
 * @return A User?
 */
suspend fun ApplicationCall.getUser(parameter: String): User? {
    val id = getPath(parameter) ?: return null

    val user = UserService.getUserById(id)

    if (user == null) {
        respond(HttpStatusCode.NotFound, "User not found")
        return null
    }

    return user
}

/**
 * "If the path parameter is not found, return null, otherwise return the post with the given id."
 *
 * The function is marked as suspend, which means that it can be called from a coroutine
 *
 * @param parameter The name of the parameter in the path.
 * @return A Post object
 */
suspend fun ApplicationCall.getPost(parameter: String): Post? {
    val id = getPath(parameter) ?: return null

    val post = PostService.getPostById(id)

    if (post == null) {
        respond(HttpStatusCode.NotFound, "Post not found")
        return null
    }

    return post
}

/**
 * It converts a string to an integer, and if it fails, it responds with a 400 Bad Request error
 *
 * @param call ApplicationCall - this is the current call that is being processed.
 * @return A nullable integer
 */
suspend fun String.toInt(call: ApplicationCall): Int? {
    val num = toIntOrNull()
    if (num == null) {
        call.respond(HttpStatusCode.BadRequest, "Cannot parse '$this' to integer")
        return null
    }
    return num
}

/**
 * Return the current time in seconds.
 *
 * @return The current time in seconds.
 */
fun currentTime(): Long {
    return System.currentTimeMillis() / 1000L
}

/** It's creating an HTTP client that will make requests to the Discord API. */
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
