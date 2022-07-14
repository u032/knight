package wiki.chess.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.chess.getForm
import wiki.chess.services.TokenService
import wiki.chess.services.UserService

fun Route.auth() {
    post("/authorize") {
        val code = call.getForm("code") ?: return@post
        val accessToken = TokenService.getToken(call, code) ?: return@post

        call.respond(accessToken)

        UserService.initializeUser(accessToken)
    }

    post("/refresh") {
        val token = call.getForm("token") ?: return@post
        val accessToken = TokenService.refreshToken(call, token) ?: return@post

        call.respond(accessToken)
    }

    post("/revoke") {
        val token = call.getForm("token") ?: return@post
        val result = TokenService.revokeToken(call, token) ?: return@post

        call.respond(result)
    }
}
