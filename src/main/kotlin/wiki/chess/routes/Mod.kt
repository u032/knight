package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.chess.db
import wiki.chess.enums.HttpError
import wiki.chess.enums.Title
import wiki.chess.getUser
import wiki.chess.validateIsModerator
import wiki.chess.validateIsNull

fun Route.mod() {
    put("/updateTitle/{user}/{title}") {
        val userId = call.parameters["user"].validateIsNull(call, HttpError.USER_PARAM) ?: return@put
        val title = call.parameters["title"].validateIsNull(call, HttpError.TITLE_PARAM) ?: return@put
        getUser(call)?.validateIsModerator(call) ?: return@put

        if (Title.valueOf(title).name.isEmpty()) {
            call.respond(HttpStatusCode.NoContent, "No content")
            return@put
        }

        getUser(call, userId) ?: return@put

        db.collection("users").document(userId).update("title", title)
        call.respond(HttpStatusCode.OK, "Title updated")
    }
    delete("/clearTitle/{user}") {
        val userId = call.parameters["user"].validateIsNull(call, HttpError.USER_PARAM) ?: return@delete

        // check user which called this route
        getUser(call)?.validateIsModerator(call) ?: return@delete

        // check user by id
        getUser(call, userId) ?: return@delete

        db.collection("users").document(userId).update("title", "")
        call.respond(HttpStatusCode.OK, "Title cleared")
    }
    delete("/deleteUser/{user}") {
        val userId = call.parameters["user"].validateIsNull(call, HttpError.USER_PARAM) ?: return@delete
        val user = getUser(call, userId) ?: return@delete

        val modUser = getUser(call)?.validateIsModerator(call) ?: return@delete

        if (modUser.id == user.id) {
            call.respond(HttpStatusCode.BadRequest, "You can't delete yourself")
            return@delete
        }

        db.collection("users").document(userId).delete()
        call.respond(HttpStatusCode.OK, "Account deleted")
    }
}
