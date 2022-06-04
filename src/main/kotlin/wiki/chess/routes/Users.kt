package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.enums.Country
import wiki.chess.enums.Federation
import wiki.chess.enums.Sex
import wiki.chess.getDiscordUser
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
                db.collection("users").document(id)
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
            val discordUser = getDiscordUser(call) ?: return@get

            val user = withContext(Dispatchers.IO) {
                db.collection("users").document(discordUser.id).get().get()
            }.toObject(User::class.java)

            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@get
            }

            call.respond(user)
        }
        put("/update/@me") {
            val discordUser = getDiscordUser(call) ?: return@put

            val user: User = call.receive()

            if (user.name.length < 2) {
                call.respond(HttpStatusCode.BadRequest, "Name length must be more than 2 characters")
                return@put
            }

            if (user.federation != Federation.FIDE.name && user.federation != Federation.NATIONAL.name) {
                call.respond(HttpStatusCode.BadRequest, "Federation must be FIDE or NATIONAL")
                return@put
            }

            if (user.sex != Sex.MALE.name && user.sex != Sex.FEMALE.name) {
                call.respond(HttpStatusCode.BadRequest, "Sex must be MALE or FEMALE")
                return@put
            }

            val data: Map<String, Any> = mapOf(
                "name" to user.name,
                "bio" to user.bio,
                "chessLink" to user.chessLink,
                "country" to user.country.ifEmpty { Country.INTERNATIONAL.name },
                "email" to user.email,
                "federation" to user.federation,
                "sex" to user.sex,
                "title" to user.title
            )

            withContext(Dispatchers.IO) {
                db.collection("users").document(discordUser.id).update(data).get()
            }

            call.respond(HttpStatusCode.OK, "Account updated")
        }
        delete("/delete/@me") {
            val discordUser = getDiscordUser(call) ?: return@delete

            withContext(Dispatchers.IO) {
                db.collection("users").document(discordUser.id).delete().get()
            }

            call.respond(HttpStatusCode.OK, "Account deleted")
        }
    }
}
