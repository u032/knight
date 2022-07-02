package wiki.chess

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import wiki.chess.enums.Role
import wiki.chess.models.User

suspend fun String?.validateIsNull(call: ApplicationCall, message: String): String? {
    if (this.isNullOrEmpty()) {
        call.respond(HttpStatusCode.BadRequest, message)
        return null
    }
    return this
}

suspend fun String?.validateIsNull(call: ApplicationCall): String? {
    return validateIsNull(call, "Required parameters or headers is missing")
}

suspend fun String.validateHasLength(call: ApplicationCall, min: Int, max: Int = 999999999): Validate? {
    if (this.length in max..min) {
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
