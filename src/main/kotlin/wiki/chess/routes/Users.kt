package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.*
import wiki.chess.enums.Federation
import wiki.chess.enums.Sex
import wiki.chess.models.User

fun Route.users() {
    get("/get") {
        val usersDocuments = withContext(Dispatchers.IO) {
            db.collection("users").get().get().documents
        }

        val users: ArrayList<User> = ArrayList()

        usersDocuments.forEach { user ->
            users.add(user.toObject(User::class.java).apply { email = "" })
        }

        call.respond(users)
    }
    get("/get/{id}") {
        val id = call.parameters["id"].validateIsNull(call) ?: return@get
        val user = getUser(call, id) ?: return@get

        user.email = ""

        call.respond(user)
    }
    get("/get/@me") {
        val user = getUser(call) ?: return@get

        call.respond(user)
    }
    put("/update/@me") {
        val discordUser = getDiscordUser(call) ?: return@put

        val user: User = call.receive()

        user.name.validateHasLength(call, 2, 32) ?: return@put

        if (user.federation != Federation.FIDE && user.federation != Federation.NATIONAL) {
            call.respond(HttpStatusCode.BadRequest, "Federation must be FIDE or NATIONAL")
            return@put
        }

        if (user.sex != Sex.MALE && user.sex != Sex.FEMALE) {
            call.respond(HttpStatusCode.BadRequest, "Sex must be MALE or FEMALE")
            return@put
        }

        val data: Map<String, Any> = mapOf(
            "name" to user.name,
            "bio" to user.bio,
            "chessLink" to user.chessLink,
            "country" to (user.country?.name ?: ""),
            "email" to user.email,
            "federation" to user.federation.name,
            "sex" to user.sex.name
        )

        db.collection("users").document(discordUser.id).update(data)

        call.respond(HttpStatusCode.OK, "Account updated")
    }
    delete("/delete/@me") {
        val discordUser = getDiscordUser(call) ?: return@delete

        db.collection("users").document(discordUser.id).delete()

        call.respond(HttpStatusCode.OK, "Account deleted")
    }
}
