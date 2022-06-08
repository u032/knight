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
import wiki.chess.validateIsNull

fun Route.mod() {
    put("/updateTitle/{user}/{title}") {
        val userId = call.parameters["user"].validateIsNull(call) ?: return@put
        val title = call.parameters["title"].validateIsNull(call) ?: return@put
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

        if (title == "RM") {
            db.collection("users").document(userId).update("title", "")
            call.respond(HttpStatusCode.OK, "Title updated")
            return@put
        }
        db.collection("users").document(userId).update("title", title)
        call.respond(HttpStatusCode.OK, "Title updated")
    }
    delete("/delete/{user}") {
        val userId = call.parameters["user"].validateIsNull(call) ?: return@delete
        val user = getUser(call, userId) ?: return@delete

        val modUser = getUser(call)

        if (modUser == null || modUser.role == Role.USER.name) {
            call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            return@delete
        }

        if (modUser.id == user.id) {
            call.respond(HttpStatusCode.BadRequest, "You can't delete yourself")
            return@delete
        }

        db.collection("users").document(userId).delete()
        call.respond(HttpStatusCode.OK, "Account deleted")
    }
}
