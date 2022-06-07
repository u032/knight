package wiki.chess.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import wiki.chess.routes.mod
import wiki.chess.routes.posts
import wiki.chess.routes.users

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        route("/users") { users() }
        route("/posts") { posts() }
        route("/mod") { mod() }
    }
}
