package wiki.chess.routes

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import wiki.chess.db
import wiki.chess.models.User
import wiki.chess.services.UserService

fun Route.notificationRoutes() {
    webSocket {
        for (frame in incoming) {
            if (frame !is Frame.Text) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Error. Frame not a text."))
                return@webSocket
            }

            val user: User? = UserService.getUserByToken(frame.readText())

            if (user == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Token invalid or user not found"))
                return@webSocket
            }

            db.collection("users").document(user.id).addSnapshotListener { value, _ ->
                if (value == null) return@addSnapshotListener

                val internalUser = value.toObject(User::class.java) ?: return@addSnapshotListener
                val data = internalUser.notifications

                runBlocking {
                    sendSerialized(data)
                }
            }
        }
    }
}
