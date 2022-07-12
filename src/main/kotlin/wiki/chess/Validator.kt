package wiki.chess

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import wiki.chess.enums.Role
import wiki.chess.models.User

suspend fun <T> T.validateIsNull(call: ApplicationCall, status: HttpStatusCode, message: String): T? {
    if (this == null) {
        call.respond(status, message)
        return null
    }
    return this
}

suspend fun String.validateHasLength(call: ApplicationCall, min: Int, max: Int = 10000): Validate? {
    if (this.length !in min..max) {
        call.respond(HttpStatusCode.BadRequest, "String length must be more than $min and less than $max")
        return null
    }
    return Validate()
}

suspend fun User.validateIsModerator(call: ApplicationCall): User? {
    if (this.role == Role.USER) {
        call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
        return null
    }
    return this
}

class Validate
