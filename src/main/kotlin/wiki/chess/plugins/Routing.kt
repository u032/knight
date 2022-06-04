package wiki.chess.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import wiki.chess.routes.posts
import wiki.chess.routes.users

fun Application.configureRouting() {
    routing {
        users()
        posts()
    }
}
