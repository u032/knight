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
import wiki.chess.getUser
import wiki.chess.models.User

fun Route.users() {
    route("/users") {
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
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Parameter id is required")
                return@get
            }

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

            if (user.name.length < 2) {
                call.respond(HttpStatusCode.BadRequest, "Name length must be more than 2 characters")
                return@put
            }

            if(user.rating < 100 || user.rating > 3750) {
                call.respond(HttpStatusCode.BadRequest, "Rating cannot be less than 100 and higher than 3750")
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
                "rating" to user.rating,
                "country" to user.country.ifEmpty { Country.UN.name },
                "email" to user.email,
                "federation" to user.federation,
                "sex" to user.sex
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
}
