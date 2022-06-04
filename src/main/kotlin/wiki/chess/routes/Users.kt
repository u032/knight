package wiki.chess.routes

import com.google.firebase.cloud.FirestoreClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.httpClient
import wiki.chess.models.DiscordUser
import wiki.chess.models.User

fun Route.users() {
    route("/users") {
        get("/get/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "400 Bad Request")
                return@get
            }

            val user = withContext(Dispatchers.IO) {
                FirestoreClient.getFirestore().collection("users").document(id)
                    .get().get()
            }.toObject(User::class.java)

            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@get
            }

            user.email = ""

            call.respond(user)
        }
        get("/get/@me") {
            val token = call.request.headers[HttpHeaders.Authorization]
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, "401")
                return@get
            }

            val discordUser: DiscordUser = httpClient.get("https://discord.com/api/users/@me") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()

            val user = withContext(Dispatchers.IO) {
                FirestoreClient.getFirestore().collection("users").document(discordUser.id).get().get()
            }.toObject(User::class.java)

            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@get
            }

            call.respond(user)
        }
    }
}
