package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.enums.Role
import wiki.chess.enums.Title
import wiki.chess.getUser
import wiki.chess.models.User

fun Route.mod() {
    route("/moderation") {
        put("/updateTitle/{user}/{title}") {
            val userId = call.parameters["user"]
            val title = call.parameters["title"]

            if (userId == null || title == null) {
                call.respond(HttpStatusCode.BadRequest, "Parameter userId and title is required")
                return@put
            }

            val modUser = getUser(call) ?: return@put

            if (modUser.role == Role.USER.name) {
                call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                return@put
            }

            if (Title.valueOf(title).name.isEmpty()) {
                call.respond(HttpStatusCode.NoContent, "No content")
                return@put
            }

            val user = withContext(Dispatchers.IO) {
                db.collection("users").document(userId).get().get()
            }.toObject(User::class.java)

            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@put
            }

            db.collection("users").document(userId.toString()).update("title", title)
            call.respond(HttpStatusCode.OK, "Title updated")
        }
    }
}
