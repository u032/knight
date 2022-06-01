package wiki.chess.plugins

import com.google.firebase.cloud.FirestoreClient
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/get/{id}") {
            val value: String = withContext(Dispatchers.IO) {
                FirestoreClient.getFirestore().collection("posts").document(call.parameters["id"]!!)
                    .get().get()
            }.get("name") as String
            call.respondText("Post: $value")
        }
        /*get("/hello") {
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
        }*/
    }
}
