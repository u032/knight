package wiki.chess

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import wiki.chess.enums.Role
import wiki.chess.models.User

/** It's an extension function that takes a generic type T, a call parameter, a status parameter, and a message
parameter. The function checks if the generic type is null, and if it is, it responds with the status and message, and
returns null. If the generic type is not null, it returns the generic type. */
suspend fun <T> T.isNull(call: ApplicationCall, status: HttpStatusCode, message: String): T? {
    if (this == null) {
        call.respond(status, message)
        return null
    }
    return this
}

/**
 * If the number is negative, respond with a 400 Bad Request error, otherwise return the number
 *
 * @param call ApplicationCall - the current call
 * @return A nullable Int
 */
suspend fun Int.isNegative(call: ApplicationCall): Int? {
    if (this <= 0) {
        call.respond(HttpStatusCode.BadRequest, "Number must not be negative")
        return null
    }
    return this
}

/**
 * "If the length of the string is not between min and max, respond with a 400 Bad Request and return null, otherwise
 * return the string."
 *
 * The function is marked as suspend, which means it can be called from a coroutine. The function takes a call parameter,
 * which is the ApplicationCall object that represents the current request. The function also takes two Int parameters, min
 * and max. The max parameter has a default value of 10000, so if you don't specify a value for max, it will default to
 * 10000
 *
 * @param call The ApplicationCall object that is passed to the route handler.
 * @param min The minimum length of the string
 * @param max The maximum length of the string.
 * @return A String?
 */
suspend fun String.hasLength(call: ApplicationCall, min: Int, max: Int = 10000): String? {
    if (this.length !in min..max) {
        call.respond(HttpStatusCode.BadRequest, "String length must be more than $min and less than $max")
        return null
    }
    return this
}

/**
 * If the user is not a moderator, return a 403 Forbidden response
 *
 * @param call ApplicationCall - the current call
 * @return The user object if the user is a moderator, null otherwise.
 */
suspend fun User.isModerator(call: ApplicationCall): User? {
    if (this.role == Role.USER) {
        call.respond(HttpStatusCode.Forbidden)
        return null
    }
    return this
}
