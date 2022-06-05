package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.getDiscordUser
import wiki.chess.models.User
import wiki.chess.enums.Title

fun Route.mod() {
    route("/moderation") {
        post("/setTitle/{user}/{title}") {
            val token = call.request.headers[HttpHeaders.Authorization]
            val userId = call.parameters["user"]
            val title = call.parameters["title"]

            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, "401")
                return@post
            }

            val discordUser = getDiscordUser(call) ?: return@post

            val ModUser = withContext(Dispatchers.IO) {
                db.collection("users").document(discordUser.id).get().get()
            }.toObject(User::class.java)


            if (ModUser == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@post
            }

            if(ModUser.role == "USER") {
                call.respond(HttpStatusCode.Unauthorized, "401")
                return@post
            }

            if(Title.valueOf(title.toString()).toString().isEmpty()) {
                call.respond(HttpStatusCode.NoContent, "204")
                return@post
            }

            val user = withContext(Dispatchers.IO) {
                db.collection("users").document(userId.toString()).get().get()
            }.toObject(User::class.java)

            if(user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@post
            }

            db.collection("users").document(userId.toString()).update("title", title)
            call.respond(200)
        }
    }
}
