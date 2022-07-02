package wiki.chess.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.chess.services.TokenService

fun Route.auth() {
    post("/authorize") {
        val accessToken = TokenService.getToken(call) ?: return@post

        call.respond(accessToken)

        TokenService.initializeUser(accessToken)
    }

    post("/refresh") {
        val accessToken = TokenService.refreshToken(call) ?: return@post

        call.respond(accessToken)
    }
}
