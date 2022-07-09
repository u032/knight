package wiki.chess.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.chess.services.TokenService
import wiki.chess.services.UserService

fun Route.auth() {
    post("/authorize") {
        val accessToken = TokenService.getToken(call) ?: return@post

        call.respond(accessToken)

        UserService.initializeUser(accessToken)
    }

    post("/refresh") {
        val accessToken = TokenService.refreshToken(call) ?: return@post

        call.respond(accessToken)
    }

    post("/revoke") {
        val result = TokenService.revokeToken(call) ?: return@post

        call.respond(result)
    }
}
