package wiki.chess.plugins

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import wiki.chess.client
import wiki.chess.model.User

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/hello") {
            val userSession = call.sessions.get<UserSession>()
            if (userSession != null) {
                val user = client.get("https://discord.com/api/users/@me") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${userSession.accessToken}")
                    }
                }.body<User>()
                    call.respondText("User: ${user.username}")
            } else {
                call.respondText("Error")
            }
        }
    }
}
