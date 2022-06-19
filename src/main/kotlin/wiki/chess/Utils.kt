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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import wiki.chess.models.DiscordUser
import wiki.chess.models.User
import wiki.chess.resources.Users

suspend fun getDiscordUser(call: ApplicationCall): DiscordUser? {
    val token = call.request.headers[HttpHeaders.Authorization]
        .validateIsNull(call, "Missing header Authorization") ?: return null

    val res = discordApi.get(Users.Me()) {
        headers.append(HttpHeaders.Authorization, "Bearer $token")
    }

    if (!res.status.value.toString().startsWith("2")) {
        call.respond(HttpStatusCode.fromValue(res.status.value), res.status.description)
        return null
    }

    return res.body()
}

suspend fun getUser(call: ApplicationCall, id: String): User? {
    val user = try {
        withContext(Dispatchers.IO) {
            db.collection("users").document(id).get().get()
        }.toObject(User::class.java)
    } catch (e: java.lang.RuntimeException) {
        println(e.message)
        call.respond(HttpStatusCode.BadRequest, "Some field is incorrect")
        return null
    }

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

val discordApi = HttpClient {
    defaultRequest {
        url("https://discord.com/api/v9/")
    }
    install(Resources)
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}