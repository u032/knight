package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.chess.getQuery
import wiki.chess.getUser
import wiki.chess.models.User
import wiki.chess.services.UserService
import wiki.chess.validateHasLength
import wiki.chess.validateIsNegative

fun Route.users() {
    get {
        val limit = call.getQuery("limit")?.toIntOrNull() ?: return@get
        val before = call.getQuery("before", false)!!

        if (limit.validateIsNegative(call, HttpStatusCode.BadRequest, "Number must not be negative"))
            return@get

        call.respond(UserService.getUsers(limit, before))
    }

    get("/{id}") {
        val user = call.getUser("id") ?: return@get

        user.email = ""

        call.respond(user)
    }

    get("/@me") {
        val user = call.getUser() ?: return@get

        call.respond(user)
    }

    put("/@me/update") {
        val user = call.getUser() ?: return@put

        val content: User = try {
            call.receive()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
            return@put
        }

        content.name.validateHasLength(call, 2, 32) ?: return@put

        val data: Map<String, Any?> = mapOf(
            "name" to content.name,
            "bio" to content.bio,
            "references" to content.references,
            "country" to content.country,
            "email" to content.email,
            "federation" to content.federation,
            "sex" to content.sex
        )

        UserService.updateUser(user, data)

        call.respond(HttpStatusCode.OK, "Account updated")
    }

    delete("/@me/delete") {
        val user = call.getUser() ?: return@delete

        UserService.deleteUser(user)

        call.respond(HttpStatusCode.OK, "Account deleted")
    }
}
