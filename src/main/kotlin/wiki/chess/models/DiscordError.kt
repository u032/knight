package wiki.chess.models

import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class DiscordError(
    @Transient var status: HttpStatusCode = HttpStatusCode.OK,
    val error: String = "",
    val error_description: String = ""
)
