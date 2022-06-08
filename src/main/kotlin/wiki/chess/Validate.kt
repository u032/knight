package wiki.chess

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun String?.validateIsNull(call: ApplicationCall): String? {
    if (this.isNullOrEmpty()) {
        call.respond(HttpStatusCode.BadRequest, "Required parameters is missing")
        return null
    }
    return this
}

suspend fun String.validateHasLength(call: ApplicationCall, min: Int, max: Int = 9999): String? {
    if (this.length in max..min) {
        call.respond(HttpStatusCode.BadRequest, "String length must be more than $min and less than $max")
        return null
    }
    return this
}
