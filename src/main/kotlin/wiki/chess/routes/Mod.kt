package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.chess.db
import wiki.chess.enums.Title
import wiki.chess.getPath
import wiki.chess.getUser
import wiki.chess.validateIsModerator

fun Route.mod() {
    put("/{user}/title/{title}") {
        val user = call.getUser()?.validateIsModerator(call) ?: return@put
        val title = call.getPath("title") ?: return@put

        if (Title.valueOf(title).name.isEmpty()) {
            call.respond(HttpStatusCode.NoContent, "No content")
            return@put
        }

        call.getUser("user") ?: return@put

        db.collection("users").document(user.id).update("title", title)
        call.respond(HttpStatusCode.OK, "Title updated")
    }

    delete("/{user}/title/clear") {
        call.getUser()?.validateIsModerator(call) ?: return@delete
        val user = call.getUser("user") ?: return@delete

        db.collection("users").document(user.id).update("title", null)
        call.respond(HttpStatusCode.OK, "Title cleared")
    }

    delete("/{user}/delete") {
        val user = call.getUser("user") ?: return@delete
        val modUser = call.getUser()?.validateIsModerator(call) ?: return@delete

        if (modUser.id == user.id) {
            call.respond(HttpStatusCode.BadRequest, "You can't delete yourself")
            return@delete
        }

        db.collection("users").document(user.id).delete()
        call.respond(HttpStatusCode.OK, "Account deleted")
    }
}
