package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.chess.*
import wiki.chess.enums.HttpError
import wiki.chess.models.User
import wiki.chess.services.UserService

fun Route.users() {
    get("/get/all") {
        call.respond(UserService.getAllUsersSafety())
    }
    get("/get/{id}") {
        val id = call.parameters["id"].validateIsNull(call, HttpError.ID_PARAM) ?: return@get
        val user = UserService.getUser(call, id) ?: return@get

        user.email = ""

        call.respond(user)
    }
    get("/get/@me") {
        val user = UserService.getUser(call) ?: return@get

        call.respond(user)
    }
    put("/update/@me") {
        val discordUser = UserService.getDiscordUser(call) ?: return@put

        val user: User = try {
            call.receive()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
            return@put
        }

        user.name.validateHasLength(call, 2, 32) ?: return@put

        val data: Map<String, Any?> = mapOf(
            "name" to user.name,
            "bio" to user.bio,
            "references" to user.references,
            "country" to user.country,
            "email" to user.email,
            "federation" to user.federation,
            "sex" to user.sex
        )

        db.collection("users").document(discordUser.id).update(data)

        call.respond(HttpStatusCode.OK, "Account updated")
    }
    delete("/delete/@me") {
        val user = UserService.getDiscordUser(call) ?: return@delete

        UserService.deleteUserById(user)

        call.respond(HttpStatusCode.OK, "Account deleted")
    }
}
