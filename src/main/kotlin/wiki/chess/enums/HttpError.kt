package wiki.chess.enums

import io.ktor.http.*

enum class HttpError(val code: HttpStatusCode, val message: String) {
    ID_PARAM(HttpStatusCode.BadRequest, "Parameter 'id' is missing"),
    USER_PARAM(HttpStatusCode.BadRequest, "Parameter 'user' is missing"),
    TITLE_PARAM(HttpStatusCode.BadRequest, "Parameter 'title' is missing"),
    POST_NOT_FOUND(HttpStatusCode.NotFound, "Post not found"),
    USER_NOT_FOUND(HttpStatusCode.NotFound, "User not found"),
    AUTH_HEADER(HttpStatusCode.BadRequest, "Missing header Authorization")
}