package wiki.chess

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import wiki.chess.enums.Role
import wiki.chess.models.User

suspend fun <T> T.isNull(call: ApplicationCall, status: HttpStatusCode, message: String): T? {
    if (this == null) {
        call.respond(status, message)
        return null
    }
    return this
}

suspend fun Int.isNegative(call: ApplicationCall): Int? {
    if (this <= 0) {
        call.respond(HttpStatusCode.BadRequest, "Number must not be negative")
        return null
    }
    return this
}

suspend fun String.hasLength(call: ApplicationCall, min: Int, max: Int = 10000): String? {
    if (this.length !in min..max) {
        call.respond(HttpStatusCode.BadRequest, "String length must be more than $min and less than $max")
        return null
    }
    return this
}

suspend fun User.isModerator(call: ApplicationCall): User? {
    if (this.role == Role.USER) {
        call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
        return null
    }
    return this
}
